package org.fengsoft.jts2geojson.convert.services;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TileGeomResult;
import com.wdtinc.mapbox_vector_tile.adapt.jts.UserDataKeyValueMapConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;
import no.ecc.vectortile.VectorTileEncoder;
import org.fengsoft.jts2geojson.convert.common.GeometryConvert;
import org.fengsoft.jts2geojson.convert.entity.GeometryEntity;
import org.fengsoft.jts2geojson.convert.tile.GlobalGeodetic;
import org.fengsoft.jts2geojson.convert.tile.GlobalMercator;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.*;
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
public class GeoJsonServicesImpl<T extends GeometryEntity<ID>, ID extends Serializable> implements GeoJsonServices<T> {
    public GeometryConvert geometryConvert = new GeometryConvert();
    public WKBReader wkbReader = new WKBReader();
    public WKBWriter wkbWriter = new WKBWriter();
    public GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    public GlobalGeodetic globalGeodetic = new GlobalGeodetic("", 256);
    public GlobalMercator globalMercator = new GlobalMercator(256);
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

    /**
     * 传入参数为TMS
     *
     * @param srsname
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double[] calBbox(String srsname, int x, int y, int z) {
        if ("4326".equals(srsname.split(":")[1])) {
            return globalGeodetic.tileLatLonBounds(x, y, z);
        } else if ("3857".equals(srsname.split(":")[1])) {
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
            clipEnvelope.expandBy((bboxs[3] - bboxs[1]) * .1f, (bboxs[2] - bboxs[0]) * .1f);
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
            e.printStackTrace();
        }
    }

    public void toMvt2(List<T> res, double[] bboxs, String layerName, Integer x, Integer y, Integer z) {
        VectorTileEncoder encoder = new VectorTileEncoder(4096, 128, true);
        double[] piexls = new double[]{x * 256, y * 256};
        for (T t : res) {
            synchronized (t) {
                if (t.getShape() instanceof PGobject) {
                    PGobject pGobject = (PGobject) t.getShape();
                    byte[] bytes = WKBReader.hexToBytes(pGobject.getValue());
                    Geometry geometry = null;
                    try {
                        if (bytes.length > 0) {
                            geometry = wkbReader.read(bytes);

                            Geometry geomPiex = geom2piex(geometry, piexls, z);
                            encoder.addFeature(layerName, transBean2Map(t), geomPiex);
                        }
                    } catch (ParseException e) {
                    }
                }
            }
        }
        try {
            Files.write(Paths.get(cachePath, layerName, String.format("%d-%d-%d", z, x, y) + ".mvt"), encoder.encode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Geometry geom2piex(Geometry geometry, double[] pxy, int z) {
        if (geometry.getGeometryType().equals("Point")) {
            new CoordinateSequences();
            return geometryFactory.createPoint(cooridinate2point(geometry.getCoordinate(), pxy, z));
        } else if (geometry.getGeometryType().equals("MultiPoint")) {
            Point[] ps = new Point[geometry.getCoordinates().length];
            for (int i = 0; i < geometry.getCoordinates().length; i++) {
                ps[i] = geometryFactory.createPoint(cooridinate2point(geometry.getCoordinates()[i], pxy, z));
            }
            return geometryFactory.createMultiPoint(ps);
        } else if (geometry.getGeometryType().equals("LineString")) {
            return lineString2pixel((LineString) geometry, pxy, z);
        } else if (geometry.getGeometryType().equals("MultiLineString")) {
            MultiLineString multiLineStringIn = (MultiLineString) geometry;
            LineString[] lineStrings = new LineString[multiLineStringIn.getNumGeometries()];
            for (int i = 0; i < multiLineStringIn.getNumGeometries(); i++) {
                lineStrings[i] = lineString2pixel((LineString) multiLineStringIn.getGeometryN(i), pxy, z);
            }
            return new MultiLineString(lineStrings, geometryFactory);
        } else if (geometry.getGeometryType().equals("Polygon")) {
            return polygon2pixel((Polygon) geometry, pxy, z);
        } else if (geometry.getGeometryType() == "MultiPolygon") {
            MultiPolygon multiPolygonIn = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multiPolygonIn.getNumGeometries()];
            for (int i = 0; i < multiPolygonIn.getNumGeometries(); i++) {
                polygons[i] = polygon2pixel((Polygon) multiPolygonIn.getGeometryN(i), pxy, z);
            }
            return new MultiPolygon(polygons, geometryFactory);
        }

        return null;
    }

    private LineString lineString2pixel(LineString lineStringIn, double[] pxy, int z) {
        Coordinate[] ps = new Coordinate[lineStringIn.getCoordinates().length];
        for (int i = 0; i < lineStringIn.getCoordinates().length; i++) {
            ps[i] = cooridinate2point(lineStringIn.getCoordinates()[i], pxy, z);
        }
        return geometryFactory.createLineString(ps);
    }

    private Polygon polygon2pixel(Polygon polygonIn, double[] pxy, int z) {
        LinearRing[] lineStrings = new LinearRing[polygonIn.getNumInteriorRing()];
        for (int i = 0; i < polygonIn.getNumInteriorRing(); i++) {
            LinearRing linearRing = (LinearRing) polygonIn.getInteriorRingN(i);
            Coordinate[] ps = new Coordinate[linearRing.getCoordinates().length];
            for (int j = 0; j < linearRing.getCoordinates().length; j++) {
                ps[j] = cooridinate2point(linearRing.getCoordinates()[j], pxy, z);
            }
            ;
            lineStrings[i] = geometryFactory.createLinearRing(ps);
        }

        Coordinate[] ps = new Coordinate[polygonIn.getExteriorRing().getCoordinates().length];
        for (int i = 0; i < polygonIn.getExteriorRing().getCoordinates().length; i++) {
            ps[i] = cooridinate2point(polygonIn.getExteriorRing().getCoordinates()[i], pxy, z);
        }
        return geometryFactory.createPolygon(geometryFactory.createLinearRing(ps), lineStrings);
    }

    private Coordinate cooridinate2point(Coordinate coordinate, double[] pxy, int z) {
        double[] pielxs = globalGeodetic.lonlatToPixels(coordinate.x, coordinate.y, z);

//        pielxs[0] = longitudeToPixelX(coordinate.x, z);
//        pielxs[1] = latitudeToPixelY(coordinate.y, z);
        return new Coordinate((int) (pielxs[0] - pxy[0]), (int) (pielxs[1] - pxy[1]));
    }

    public double longitudeToPixelX(double longitude, int zoom) {
        return (longitude + 180) / 360 * ((long) 256 << zoom);
    }

    public static double latitudeToPixelY(double latitude, int zoom) {
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        return (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) * ((long) 256 << zoom);
    }
}
