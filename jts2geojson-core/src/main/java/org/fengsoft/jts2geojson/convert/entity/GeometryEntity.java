package org.fengsoft.jts2geojson.convert.entity;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:52
 */
public interface GeometryEntity<ID> {
    ID getId();

    Object getShape();

    void setShape(Object object);
}
