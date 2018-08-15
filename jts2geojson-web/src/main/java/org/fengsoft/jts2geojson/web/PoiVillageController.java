package org.fengsoft.jts2geojson.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fengsoft.jts2geojson.services.PoiVillageServices;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Author JerFer
 * @Date 2018/7/30---16:48
 */
@Controller
@RequestMapping(value = "poiVillage")
public class PoiVillageController {
    @Autowired
    private PoiVillageServices poiVillageServices;

    @RequestMapping("poi")
    @ResponseBody
    public String getLine(String srsname, String bbox) {
        try {
            return poiVillageServices.allFeatures(srsname, bbox);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }


    @RequestMapping(value = "poi2/{z}/{x}/{y}.mvt",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void getLine2(@RequestParam("srsname") String srsname,
                         @PathVariable("x") Integer x,
                         @PathVariable("y") Integer y,
                         @PathVariable("z") Integer z,
                         HttpServletResponse response) {
        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            OutputStream os = response.getOutputStream();
            os.write(poiVillageServices.listFeature(srsname, x, y, z));
            os.flush();
            os.close();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
