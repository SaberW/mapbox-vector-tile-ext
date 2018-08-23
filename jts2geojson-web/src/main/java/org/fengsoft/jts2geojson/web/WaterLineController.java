package org.fengsoft.jts2geojson.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.fengsoft.jts2geojson.common.AnotherException;
import org.fengsoft.jts2geojson.services.WaterLineServices;
import org.fengsoft.jts2geojson.web.base.VectorTileController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @Author JerFer
 * @Date 2018/7/30---16:48
 */
@Controller
@RequestMapping(value = "waterLine")
public class WaterLineController extends VectorTileController {
    @Autowired
    private WaterLineServices waterLineServices;

    @RequestMapping("line")
    @ResponseBody
    public String getLine(String srsname, String bbox) {
        try {
            return waterLineServices.allFeatures(srsname, bbox);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }


    @RequestMapping(value = "line2/{z}/{x}/{y}.mvt",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ExceptionHandler(AnotherException.class)
    public String getLine2(@RequestParam("srsname") String srsname,
                         @RequestParam("layerName") String layerName,
                         @PathVariable("x") Integer x,
                         @PathVariable("y") Integer y,
                         @PathVariable("z") Integer z,
                         HttpServletResponse response) {
        File parentFile = new File(cachePath + File.separator + layerName);
        if (!parentFile.exists()) parentFile.mkdir();
        File file = new File(cachePath + File.separator + layerName, String.format("%d-%d-%d", z, x, y) + ".mvt");
        if (!file.exists()) {
            waterLineServices.listFeature(srsname, layerName, x, y, z);
        }
        return "forward:/waterLine/download/" + layerName + "/" + file.getName();
    }
}
