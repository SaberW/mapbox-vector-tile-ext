package org.fengsoft.jts2geojson.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author JerFer
 * @Date 2018/8/2---13:00
 */
@Controller
public class IndexController {
    @RequestMapping(value = "index")
    public ModelAndView index(HttpServletRequest request, @RequestParam(value = "code", defaultValue = "vectortile") String code) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        if (code.equals("tiledownload")) {
            modelAndView.addObject("content", "model/tiledownload.html");
            modelAndView.addObject("js", "tiledownload");
        } else if (code.equals("vectortile")) {
            modelAndView.addObject("content", "model/vectortile.html");
            modelAndView.addObject("js", "vectortile");
        }
        return modelAndView;
    }
}
