package org.fengsoft.jts2geojson.web;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
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
@RequestMapping(value = "tilecache")
public class TileCacheController {
    @Value("${cache.image-tile-path}")
    private String imageTilePath;
    @Autowired
    private GlobalMercator mercator;


    @RequestMapping(value = "tile/{x}/{y}/{z}",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> tile(@PathVariable("x") Integer x, @PathVariable("y") Integer y, @PathVariable("z") Integer z) {

        String filePath = imageTilePath + File.separator + z + File.separator + String.format("%d-%d.%s", x, y, "png");
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
