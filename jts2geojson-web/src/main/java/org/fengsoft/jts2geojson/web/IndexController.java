package org.fengsoft.jts2geojson.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author JerFer
 * @Date 2018/8/2---13:00
 */
@Controller
public class IndexController {
    @RequestMapping(value = "index")
    public String index(Model model, HttpServletRequest request) {
        return "index";
    }
}
