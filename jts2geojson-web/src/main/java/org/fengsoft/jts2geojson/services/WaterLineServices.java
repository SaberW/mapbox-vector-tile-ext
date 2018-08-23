package org.fengsoft.jts2geojson.services;

import cn.com.enersun.dgpmicro.service.GeoJsonServicesImpl;
import cn.com.enersun.dgpmicro.services.VectorTileServicesImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLReady;
import org.fengsoft.jts2geojson.entity.WaterLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:43
 */
@Service
@Transactional
@Slf4j
public class WaterLineServices extends GeoJsonServicesImpl<WaterLine, Integer> , VectorTileServicesImpl<WaterLine,Integer> {
    @Autowired
    private SQLManager sqlManager;

    public String allFeatures(String srsname, String bbox) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(toFeatures(sqlManager.all(WaterLine.class)));
    }

    public void listFeature(String srsname, String layerName, Integer x, Integer y, Integer z) {
        //计算范围
        double[] bboxs = calBbox(srsname, x, y, z);
        String sql = "SELECT t.* FROM " + layerName + " t  WHERE ST_Intersects (shape,ST_MakeEnvelope(" + bboxs[1] + "," + bboxs[0] + "," + bboxs[3] + "," + bboxs[2] + "," + srsname.split(":")[1] + "))";
        SQLReady sqlReady = new SQLReady(sql);
        List<WaterLine> res = sqlManager.execute(sqlReady, WaterLine.class);

        toMvt(res, bboxs, layerName, x, y, z);
    }
}
