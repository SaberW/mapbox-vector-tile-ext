package org.fengsoft.jts2geojson.convert.services;

import org.geojson.Feature;
import java.io.IOException;
import java.util.List;

/**
 * @Author JerFer
 * @Date 2018/8/1---10:24
 */
public interface GeoJsonServices<T> {
    Feature toFeature(T t) throws Exception;

    void toEntity(Feature feature, T t) throws IOException;

    void toMvt(List<T> res, double[] bboxs, String layerName, Integer x, Integer y, Integer z);
}
