package org.fengsoft.jts2geojson.generate;

import org.beetl.sql.core.*;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.PostgresStyle;
import org.beetl.sql.core.db.SQLiteStyle;
import org.beetl.sql.ext.DebugInterceptor;

import java.util.ArrayList;
import java.util.List;

public class BeetlGenerate2 {
    public static void main(String[] args) {

        ConnectionSource source = ConnectionSourceHelper.getSimple(
                "org.sqlite.JDBC",
                "jdbc:sqlite:C:\\Users\\Administrator\\Desktop\\empt.mbtiles", "", "");
        DBStyle mysql = new SQLiteStyle();
        // sql语句放在classpagth的/sql 目录下
        SQLLoader loader = new ClasspathLoader("/sql");
        // 数据库命名跟java命名一样，所以采用DefaultNameConversion，还有一个是UnderlinedNameConversion，下划线风格的，
        UnderlinedNameConversion nc = new UnderlinedNameConversion();
        // 最后，创建一个SQLManager,DebugInterceptor 不是必须的，但可以通过它查看sql执行情况
        SQLManager sqlManager = new SQLManager(mysql, loader, source, nc, new Interceptor[]{new DebugInterceptor()});

        List<String> tabNames = new ArrayList<>();
        tabNames.add("images");
        tabNames.add("images_transparency");
        tabNames.add("map");
        tabNames.add("metadata");
        for (String string : tabNames) {
            try {
                sqlManager.genPojoCode(string, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
