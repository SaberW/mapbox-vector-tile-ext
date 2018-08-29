package org.fengsoft.jts2geojson.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.fengsoft.jts2geojson.common.AnotherException;
import org.fengsoft.jts2geojson.services.RegionCountyServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping(value = "vt")
public class VectorTileController {
    @Autowired
    private RegionCountyServices regionCountyServices;

    @Value("${cache.vector-tile-path}")
    public String cachePath;


    @RequestMapping("polygon")
    @ResponseBody
    public String getLine(String srsname, String bbox) {
        try {
            return regionCountyServices.allFeatures(srsname, bbox);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     *  进来的是XYZ scheme
     * @param srsname
     * @param layerName
     * @param x
     * @param y
     * @param z
     * @return
     */
    @RequestMapping(value = "polygon2/{z}/{x}/{y}.mvt",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ExceptionHandler(AnotherException.class)
    public String getLine2(@RequestParam("srsname") String srsname,
                           @RequestParam("layerName") String layerName,
                           @PathVariable("x") Integer x,
                           @PathVariable("y") Integer y,
                           @PathVariable("z") Integer z) {
        File parentFile = new File(cachePath + File.separator + layerName);
        if (!parentFile.exists()) parentFile.mkdir();

        //y = (int) Math.pow(2, z) - 1 - y;// TMS转XYZ
        //y = (1 << z) - y - 1;            //将XYZ 转为 TMS

        File file = new File(cachePath + File.separator + layerName, String.format("%d-%d-%d", z, x, y) + ".mvt");
        if (!file.exists()) {
            regionCountyServices.listFeature(srsname, layerName, x, y, z);
        }
        return "forward:/vt/download/" + layerName + "/" + file.getName();
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
        FileSystemResource file = new FileSystemResource(filePath);
        return ResponseEntity.ok().contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(new InputStreamResource(file.getInputStream()));
    }
}
