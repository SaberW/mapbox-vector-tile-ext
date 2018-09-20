package org.fengsoft.jts2geojson.service;

import org.fengsoft.geojson.common.GeoEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @Author JerFer
 * @Date 2018/8/1---10:24
 */
public abstract class VectorTileServices<T extends GeoEntity<ID>, ID extends Serializable> {
    public abstract byte[] toMapBoxMvt(List<T> res, double[] bboxs, String layerName, Integer x, Integer y, Integer z);
}
