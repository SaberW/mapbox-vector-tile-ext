package org.fengsoft.jts2geojson.web;

import org.fengsoft.jts2geojson.common.TileType;
import org.fengsoft.jts2geojson.services.GenerateTileService;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping(value = "generate")
public class GenerateTileController {
    @Autowired
    private GenerateTileService generateTileService;
    @Value("${cache.image-tile-path}")
    private String imageTilePath;

    /**
     * 102.7687099999999987, 102.7813691098872226, 25.0036402053958895, 25.0139100000000028
     *
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     */
    @RequestMapping(value = "download")
    public void tile(String tileName, Double xmin, Double xmax, Double ymin, Double ymax) {
        Envelope envelope = new Envelope(xmin, xmax, ymin, ymax);
        generateTileService.run(tileName, envelope, "EPSG:4326", TileType.TDTVEC);
    }

    @RequestMapping(value = "tile/{tileName}/{x}/{y}/{z}",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> tile(@PathVariable("tileName") String tileName, @PathVariable("x") Integer x, @PathVariable("y") Integer y, @PathVariable("z") Integer z) {

        String filePath = imageTilePath + File.separator + tileName + File.separator + z + File.separator + String.format("%d-%d.%s", x, y, "png");
        if (!new File(filePath).exists()) {
            filePath = this.getClass().getResource("/static/img/nodata.png").getPath();
        }
        try {
            FileSystemResource file = new FileSystemResource(filePath);
            return ResponseEntity.ok().contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(new InputStreamResource(file.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
