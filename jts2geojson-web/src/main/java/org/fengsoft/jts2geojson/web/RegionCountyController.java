package org.fengsoft.jts2geojson.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.fengsoft.jts2geojson.common.AnotherException;
import org.fengsoft.jts2geojson.convert.web.VectorTileController;
import org.fengsoft.jts2geojson.services.RegionCountyServices;
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
@RequestMapping(value = "regionCounty")
public class RegionCountyController extends VectorTileController {
    @Autowired
    private RegionCountyServices regionCountyServices;

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
        return "forward:/regionCounty/download/" + layerName + "/" + file.getName();
    }
}
