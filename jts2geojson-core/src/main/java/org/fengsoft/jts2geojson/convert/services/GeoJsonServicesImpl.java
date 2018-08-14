package org.fengsoft.jts2geojson.convert.services;

import org.fengsoft.jts2geojson.convert.common.GeometryConvert;
import org.fengsoft.jts2geojson.convert.entity.GeometryEntity;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.postgresql.util.PGobject;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:38
 */
public class GeoJsonServicesImpl<T extends GeometryEntity<ID>, ID extends Serializable> implements GeoJsonServices<T> {
    public GeometryConvert geometryConvert = new GeometryConvert();
    public WKBReader wkbReader = new WKBReader();
    public WKBWriter wkbWriter = new WKBWriter();
    public GeometryFactory geometryFactory = new GeometryFactory();

    public Feature toFeature(T t) throws Exception {
        Feature feature = new Feature();
        PGobject pGeobject = (PGobject) t.getShape();
        try {
            Geometry geometry = wkbReader.read(WKBReader.hexToBytes(pGeobject.getValue()));
            feature.setGeometry(geometryConvert.geometrySerializer(geometry));
            feature.setId(String.valueOf(t.getId()));
            //反射获取
            ReflectionUtils.doWithFields(t.getClass(), field -> {
                Method getMethod = ReflectionUtils.findMethod(t.getClass(), "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                if (getMethod != null)
                    feature.setProperty(field.getName(), ReflectionUtils.invokeMethod(getMethod, t));
                feature.setId(String.valueOf(t.getId()));
            });
            return feature;
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void toEntity(Feature feature, T t) throws IOException {
        try {
            PGobject pGeobject = new PGobject();
            Geometry geometry = geometryConvert.geometryDeserialize(feature.getGeometry());
            pGeobject.setValue(WKBWriter.toHex(wkbWriter.write(geometry)));
            pGeobject.setType("geometry");
            t.setShape(pGeobject);

            BeanUtils.copyProperties(feature.getProperties(), t, "shape");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FeatureCollection toFeatures(List<T> entities) {
        FeatureCollection collection = new FeatureCollection();
        collection.addAll(entities.stream().map(a -> {
            try {
                return toFeature(a);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(a -> a != null).collect(Collectors.toList()));
        return collection;
    }

    public List<T> toEntities(FeatureCollection features, Class t) {
        return features.getFeatures().stream().map(a -> {
            try {
                T entity = (T) t.newInstance();
                toEntity(a, entity);
                return entity;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(a -> a != null).collect(Collectors.toList());
    }
}
