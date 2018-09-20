package org.fengsoft.jts2geojson.web;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLReady;
import org.fengsoft.geojson.common.AnotherException;
import org.fengsoft.geojson.common.GlobalGeodetic;
import org.fengsoft.geojson.entity.RegionCounty;
import org.fengsoft.jts2geojson.service.VectorTileServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping(value = "mapbox")
public class MapBoxVTController extends VectorTileServicesImpl<RegionCounty, Integer> {
    @Autowired
    @Qualifier("sqlManagerFactoryBeanPG")
    private SQLManager sqlManager;

    @Value("${cache.vector-tile-mapbox-path}")
    public String cachePath;

    private GlobalGeodetic globalGeodetic = new GlobalGeodetic("", 256);


    /**
     * 进来的是XYZ scheme
     *
     * @param layerName
     * @param x
     * @param y
     * @param z
     * @return
     */
    @RequestMapping(value = "vt/{z}/{x}/{y}.mvt",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ExceptionHandler(AnotherException.class)
    public String getLine2(@RequestParam("layerName") String layerName,
                           @PathVariable("x") Integer x,
                           @PathVariable("y") Integer y,
                           @PathVariable("z") Integer z) {
        File parentFile = new File(cachePath + File.separator + layerName);
        if (!parentFile.exists()) parentFile.mkdir();

        //y = (int) Math.pow(2, z) - 1 - y;// TMS转XYZ
        //y = (1 << z) - y - 1;            //将XYZ 转为 TMS

        File file = new File(cachePath + File.separator + layerName, String.format("%d-%d-%d", z, x, y) + ".mvt");
        if (!file.exists()) {
            //计算范围
            double[] bboxs = globalGeodetic.tileLatLonBounds(x, y, z);
            String sql = "SELECT t.id,t.name,t.shape FROM " + layerName + " t  WHERE ST_Intersects (shape,ST_MakeEnvelope(" + bboxs[1] + "," + bboxs[0] + "," + bboxs[3] + "," + bboxs[2] + ",4326))";
            SQLReady sqlReady = new SQLReady(sql);
            List<RegionCounty> res = sqlManager.execute(sqlReady, RegionCounty.class);
            try {
                if (res.size() > 0) {
                    byte[] content = toMapBoxMvt(res, bboxs, layerName, x, y, z);
                    Files.write(Paths.get(cachePath, layerName, String.format("%d-%d-%d", z, x, y) + ".mvt"), content);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "forward:/mapbox/download/" + layerName + "/" + file.getName();
    }

    @RequestMapping(
            value = "download/{layerName}/{fileName}",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable(value = "layerName") String layerName,
            @PathVariable(value = "fileName") String fileName
    )
            throws IOException {

        String filePath = cachePath + File.separator + layerName + File.separator + fileName;
        if (new File(filePath).exists()) {
            FileSystemResource file = new FileSystemResource(filePath);
            return ResponseEntity.ok().contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(new InputStreamResource(file.getInputStream()));
        } else return ResponseEntity.noContent().build();

    }
}
