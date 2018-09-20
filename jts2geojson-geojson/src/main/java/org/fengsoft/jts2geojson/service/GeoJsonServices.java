package org.fengsoft.jts2geojson.service;

import org.geojson.Feature;

/**
 * @Author JerFer
 * @Date 2018/8/1---10:24
 */
public interface GeoJsonServices<T> {
    Feature toFeature(T t);

    void toEntity(Feature feature, T t);
}
