package org.fengsoft.jts2geojson.convert.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

/**
 * @Author JerFer
 * @Date 2018/8/15---10:55
 */
public class VectorTileController {
    @Value("${cache.path}")
    public String cachePath;

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

    @RequestMapping("saveFeature")
    @ResponseBody
    public String saveFeature(@RequestParam String geoJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Feature feature = objectMapper.readValue(geoJson, Feature.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping("saveFeatures")
    @ResponseBody
    public String saveFeatures(@RequestParam String geoJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            FeatureCollection featureCollection = objectMapper.readValue(geoJson, FeatureCollection.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
