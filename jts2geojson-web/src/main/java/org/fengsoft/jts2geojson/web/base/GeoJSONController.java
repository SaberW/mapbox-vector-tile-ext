package org.fengsoft.jts2geojson.web.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * @Author JerFer
 * @Date 2018/8/23---13:24
 */
public class GeoJSONController {
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
