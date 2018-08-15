package org.fengsoft.jts2geojson.convert.services;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.*;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;
import lombok.extern.slf4j.Slf4j;
import org.fengsoft.jts2geojson.convert.common.GeometryConvert;
import org.fengsoft.jts2geojson.convert.entity.GeometryEntity;
import org.fengsoft.jts2geojson.convert.tile.GlobalGeodetic;
import org.fengsoft.jts2geojson.convert.tile.GlobalMercator;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.postgresql.util.PGobject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ReflectionUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:38
 */
@Slf4j
public class GeoJsonServicesImpl<T extends GeometryEntity<ID>, ID extends Serializable> implements GeoJsonServices<T> {
    public GeometryConvert geometryConvert = new GeometryConvert();
    public WKBReader wkbReader = new WKBReader();
    public WKBWriter wkbWriter = new WKBWriter();
    public GeometryFactory geometryFactory = new GeometryFactory();
    public GlobalGeodetic globalGeodetic;
    public GlobalMercator globalMercator;
    private MvtLayerProps layerProps = new MvtLayerProps();
    private MvtLayerParams layerParams = new MvtLayerParams();

    @Value("${cache.path}")
    public String cachePath;

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

    public double[] calBbox(String srsname, int x, int y, int z) {
        if ("4326".equals(srsname.split(":")[1])) {
            globalGeodetic = new GlobalGeodetic("", 256);
            geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            return globalGeodetic.tileLatLonBounds(x, y, z);
        } else if ("3857".equals(srsname.split(":")[1])) {
            globalMercator = new GlobalMercator(256);
            geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
            return globalMercator.tileLatLonBounds(x, y, z);
        } else return new double[4];
    }

    private Map<String, Object> transBean2Map(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                // 过滤class属性
                if (!key.equals("class") && !key.equals("shape")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            System.out.println("transBean2Map Error " + e);
        }
        return map;
    }

    public VectorTile.Tile.GeomType toGeometrys(List<T> res, List<Geometry> geometries) {
        VectorTile.Tile.GeomType geomType = null;
        for (T t : res) {
            if (t.getShape() instanceof PGobject) {
                PGobject pGobject = (PGobject) t.getShape();
                byte[] bytes = WKBReader.hexToBytes(pGobject.getValue());
                Geometry geometry = null;
                try {
                    geometry = wkbReader.read(bytes);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (geometry != null) {
                    if (geomType == null) geomType = JtsAdapter.toGeomType(geometry);
                    geometry.setUserData(transBean2Map(t));
                    geometries.add(geometry);
                }
            }
        }
        return geomType;
    }

    public void toMvt(List<T> res, double[] bboxs, String layerName, Integer x, Integer y, Integer z) {
        final Envelope tileEnvelope = new Envelope(bboxs[1], bboxs[3], bboxs[0], bboxs[2]);
        List<Geometry> geometries = new ArrayList<>();
        VectorTile.Tile.GeomType geomType = toGeometrys(res, geometries);
        TileGeomResult tileGeom = null;
        if (geomType == VectorTile.Tile.GeomType.POLYGON) {
            Envelope clipEnvelope = new Envelope(tileEnvelope);
            clipEnvelope.expandBy((bboxs[3] - bboxs[1]) * .01f, (bboxs[2] - bboxs[0]) * .01f);
            tileGeom = JtsAdapter.createTileGeom(geometries, tileEnvelope, clipEnvelope, geometryFactory, layerParams, geom -> true);
        } else {
            tileGeom = JtsAdapter.createTileGeom(geometries, tileEnvelope, geometryFactory, layerParams, geom -> true);
        }
        // Build MVT
        VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();
        // Create MVT layer
        VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder(layerName, new MvtLayerParams());
        // MVT tile geometry to MVT features
        List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, new UserDataKeyValueMapConverter("id"));
        layerBuilder.addAllFeatures(features);
        MvtLayerBuild.writeProps(layerBuilder, layerProps);
        // Build MVT layer
        VectorTile.Tile.Layer layer = layerBuilder.build();
        // Add built layer to MVT
        tileBuilder.addLayers(layer);
        /// Build MVT
        VectorTile.Tile mvt = tileBuilder.build();
        try {
            Files.write(Paths.get(cachePath, layerName, String.format("%d-%d-%d", z, x, y) + ".mvt"), mvt.toByteArray());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
