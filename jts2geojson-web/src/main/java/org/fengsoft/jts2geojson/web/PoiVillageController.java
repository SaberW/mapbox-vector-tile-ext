package org.fengsoft.jts2geojson.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.fengsoft.jts2geojson.common.AnotherException;
import org.fengsoft.jts2geojson.convert.web.VectorTileController;
import org.fengsoft.jts2geojson.services.PoiVillageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * @Author JerFer
 * @Date 2018/7/30---16:48
 */
@Controller
@RequestMapping(value = "poiVillage")
public class PoiVillageController extends VectorTileController {
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
    @ExceptionHandler(AnotherException.class)
    public String getLine2(@RequestParam("srsname") String srsname,
                         @RequestParam("layerName") String layerName,
                         @PathVariable("x") Integer x,
                         @PathVariable("y") Integer y,
                         @PathVariable("z") Integer z) {
        File parentFile = new File(cachePath + File.separator + layerName);
        if (!parentFile.exists()) parentFile.mkdir();
        File file = new File(cachePath + File.separator + layerName, String.format("%d-%d-%d", z, x, y) + ".mvt");
        if (!file.exists()) {
            poiVillageServices.listFeature(srsname, layerName, x, y, z);
        }
        return "forward:/poiVillage/download/" + layerName + "/" + file.getName();
    }
}
