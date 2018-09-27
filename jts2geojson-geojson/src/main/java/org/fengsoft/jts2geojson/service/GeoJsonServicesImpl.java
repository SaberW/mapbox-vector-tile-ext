package org.fengsoft.jts2geojson.service;

import org.fengsoft.geojson.common.GeoEntity;
import org.fengsoft.jts2geojson.common.GeometryConvert;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.postgresql.util.PGobject;
import org.springframework.util.ReflectionUtils;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:38
 */
public class GeoJsonServicesImpl<T extends GeoEntity<ID>, ID extends Serializable> implements GeoJsonServices<T> {
    public GeometryConvert geometryConvert = new GeometryConvert();
    public WKBReader wkbReader = new WKBReader();
    public WKBWriter wkbWriter = new WKBWriter(2,true);

    public Feature toFeature(T t) {
        Feature feature = new Feature();
        GeoJsonObject geometry = null;
        if (t.getShape() instanceof PGobject) {
            PGobject pGeobject = (PGobject) t.getShape();
            try {
                geometry = geometryConvert.geometrySerializer(wkbReader.read(WKBReader.hexToBytes(pGeobject.getValue())));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        feature.setId(String.valueOf(t.getId()));
        if (geometry != null) feature.setGeometry(geometry);
        //反射获取
        ReflectionUtils.doWithFields(t.getClass(), field -> {
            if (field.getName().toLowerCase() != "shape") {
                Method getMethod = ReflectionUtils.findMethod(t.getClass(), "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                if (getMethod != null)
                    feature.setProperty(field.getName(), ReflectionUtils.invokeMethod(getMethod, t));
                feature.setId(String.valueOf(t.getId()));
            }
        });
        return feature;
    }

    public void toEntity(Feature feature, T t) {
        try {
            PGobject pGeobject = new PGobject();
            Geometry geometry = geometryConvert.geometryDeserialize(feature.getGeometry());
            geometry.setSRID(4326);
            pGeobject.setValue(WKBWriter.toHex(wkbWriter.write(geometry)));
            pGeobject.setType("geometry");
            t.setShape(pGeobject);
            org.apache.commons.beanutils.BeanUtils.populate(t, feature.getProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FeatureCollection toFeatures(List<T> entities) {
        FeatureCollection collection = new FeatureCollection();
        collection.addAll(entities.stream().map(a -> toFeature(a)).filter(a -> a != null).collect(Collectors.toList()));
        return collection;
    }

    public List<T> toEntities(FeatureCollection features, Class t) {
        return features.getFeatures().stream().map(a -> {
            try {
                T entity = (T) t.newInstance();
                toEntity(a, entity);
                return entity;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(a -> a != null).collect(Collectors.toList());
    }
}
