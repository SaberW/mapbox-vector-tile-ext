package org.fengsoft.geojson.common;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:52
 */
public interface GeoEntity<ID> {
    ID getId();

    Object getShape();

    void setShape(Object object);
}
