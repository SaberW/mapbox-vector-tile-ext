package org.fengsoft.jts2geojson.common;

import org.beetl.core.Context;
import org.beetl.core.Function;

public class LevelInfoFunction implements Function {

    @Override
    public String call(Object[] paras, Context ctx) {
        Integer level = (Integer) paras[0];
        switch (level) {
            case 1:
                return "幼儿宝宝";
            case 2:
                return "小学生";
            case 3:
                return "中学生";
            case 4:
                return "大学生";
            default:
                return "导师";
        }
    }
}
