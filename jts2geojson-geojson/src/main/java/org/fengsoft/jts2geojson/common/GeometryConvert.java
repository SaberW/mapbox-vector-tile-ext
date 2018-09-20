package org.fengsoft.jts2geojson.common;


import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class GeometryConvert {

    private GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public GeometryConvert() {
    }

    /**
     * 通用坐标转换
     *
     * @param coordinates
     * @return
     */
    private List<LngLatAlt> coordinatesToLngLatAltsList(Coordinate[] coordinates) {
        return Arrays.stream(coordinates).map(coordinate -> new LngLatAlt(coordinate.x, coordinate.y)).collect(Collectors.toList());
    }

    /**
     * 点对象转换成JSON Point
     *
     * @param point
     * @return
     */
    private org.geojson.Point pointSerializable(Point point) {
        return new org.geojson.Point(point.getX(), point.getY(), 0);
    }

    /**
     * 线对象转换成JSON lineString
     *
     * @param lineString
     * @return
     */
    private org.geojson.LineString lineStringSerializable(LineString lineString) {
        List<LngLatAlt> lngLatAltList = coordinatesToLngLatAltsList(lineString.getCoordinates());
        return new org.geojson.LineString(lngLatAltList.stream().toArray(LngLatAlt[]::new));
    }

    /**
     * 线对象转换成JSON polygon
     *
     * @param polygon
     * @return
     */
    private org.geojson.Polygon polygonSerializable(Polygon polygon) {
        org.geojson.Polygon polygonOut = new org.geojson.Polygon();
        int ringCount = polygon.getNumInteriorRing();
        for (int i = 0; i < ringCount; i++) {
            List<LngLatAlt> lngLatAltList = coordinatesToLngLatAltsList(polygon.getInteriorRingN(i).getCoordinates());
            polygonOut.addInteriorRing(lngLatAltList.stream().toArray(LngLatAlt[]::new));
        }
        LineString lineString = polygon.getExteriorRing();
        List<LngLatAlt> lngLatAltList = Arrays.stream(lineString.getCoordinates()).map(coordinate -> new LngLatAlt(coordinate.x, coordinate.y)).collect(Collectors.toList());
        polygonOut.setExteriorRing(lngLatAltList);

        return polygonOut;
    }

    /**
     * 多点对象转换成JSON multiPoint
     *
     * @param multiPoint
     * @return
     */
    private org.geojson.MultiPoint multiPointSerializable(MultiPoint multiPoint) {
        List<LngLatAlt> lngLatAlts = coordinatesToLngLatAltsList(multiPoint.getCoordinates());
        return new org.geojson.MultiPoint(lngLatAlts.stream().toArray(LngLatAlt[]::new));
    }

    /**
     * 多线对象转换成JSON multiLineString
     *
     * @param multiLineString
     * @return
     */
    private org.geojson.MultiLineString multiLineStringSerializable(MultiLineString multiLineString) {
        return new org.geojson.MultiLineString(coordinatesToLngLatAltsList(multiLineString.getCoordinates()));
    }

    /**
     * 多面对象转换成JSON multiPolygon
     *
     * @param multiPolygon
     * @return
     */
    private org.geojson.MultiPolygon multiPolygonSerializable(MultiPolygon multiPolygon) {
        org.geojson.MultiPolygon result = new org.geojson.MultiPolygon();
        int count = multiPolygon.getNumGeometries();
        for (int i = 0; i < count; i++) {
            Geometry geometry = multiPolygon.getGeometryN(i);
            if (geometry instanceof Polygon) {
                result.add(polygonSerializable((Polygon) geometry));
            }
        }
        return result;
    }

    private org.geojson.GeometryCollection geometryCollectionSerializable(GeometryCollection geometryCollection) {
        org.geojson.GeometryCollection gc = new org.geojson.GeometryCollection();
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry geometry = geometryCollection.getGeometryN(i);
            try {
                gc.add(geometrySerializer(geometry));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gc;
    }

    public GeoJsonObject geometrySerializer(Geometry geometry) throws Exception {
        if (geometry.getClass() == Point.class) {
            return pointSerializable((Point) geometry);
        } else if (geometry.getClass() == LineString.class) {
            return lineStringSerializable((LineString) geometry);
        } else if (geometry.getClass() == Polygon.class) {
            return polygonSerializable((Polygon) geometry);
        } else if (geometry.getClass() == MultiPoint.class) {
            return multiPointSerializable((MultiPoint) geometry);
        } else if (geometry.getClass() == MultiLineString.class) {
            return multiLineStringSerializable((MultiLineString) geometry);
        } else if (geometry.getClass() == MultiPolygon.class) {
            return multiPolygonSerializable((MultiPolygon) geometry);
        } else if (geometry.getClass() == GeometryCollection.class) {
            return geometryCollectionSerializable((GeometryCollection) geometry);
        } else {
            throw new Exception("非法的输入数据");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Coordinate[] lngLatAltToCoordinates(List<LngLatAlt> coordinates) {
        return coordinates.stream().map(a -> new Coordinate(a.getLongitude(), a.getLatitude())).toArray(Coordinate[]::new);
    }

    private Point pointDeserialize(org.geojson.Point point) {
        return new Point(new Coordinate(point.getCoordinates().getLongitude(), point.getCoordinates().getLatitude()), geometryFactory.getPrecisionModel(), geometryFactory.getSRID());
    }

    private LineString lineStringDeserialize(org.geojson.LineString lineString) {
        Coordinate[] coordinates = lngLatAltToCoordinates(lineString.getCoordinates());
        return new LineString(coordinates, geometryFactory.getPrecisionModel(), geometryFactory.getSRID());
    }

    private Polygon polygonDeserialize(org.geojson.Polygon polygon) {
        LinearRing linearRing = new LinearRing(new CoordinateArraySequence(lngLatAltToCoordinates(polygon.getExteriorRing())), geometryFactory);
        List<List<LngLatAlt>> coordinates1 = polygon.getInteriorRings();
        LinearRing[] linearRings = coordinates1.stream().map(item -> new LinearRing(new CoordinateArraySequence(lngLatAltToCoordinates(item)), geometryFactory)).toArray(LinearRing[]::new);
        return new Polygon(linearRing, linearRings, geometryFactory);
    }

    private MultiPoint multiPointDeserialize(org.geojson.MultiPoint multiPoint) {
        Point[] points = multiPoint.getCoordinates().stream().map(item -> pointDeserialize(new org.geojson.Point(item))).toArray(Point[]::new);
        return new MultiPoint(points, geometryFactory);
    }

    private MultiLineString multiLineStringDeserialize(org.geojson.MultiLineString multiLineString) {
        List<LineString> lineStrings = multiLineString.getCoordinates().stream().map(item -> {
            return lineStringDeserialize(new org.geojson.LineString(item.stream().toArray(LngLatAlt[]::new)));
        }).collect(Collectors.toList());
        return new MultiLineString(lineStrings.stream().toArray(LineString[]::new), geometryFactory);
    }

    private MultiPolygon multiPolygonDeserialize(org.geojson.MultiPolygon multiPolygon) {
        List<Polygon> lineStrings = multiPolygon.getCoordinates().stream().map(item -> {
            org.geojson.Polygon p = null;
            if (item.size() == 1) {
                p = new org.geojson.Polygon(item.get(0).stream().toArray(LngLatAlt[]::new));
            } else {
                p = new org.geojson.Polygon(item.get(0).stream().toArray(LngLatAlt[]::new));
                for (int i = 1; i < item.size(); i++) {
                    p.addInteriorRing(item.get(i).stream().toArray(LngLatAlt[]::new));
                }
            }
            return polygonDeserialize(p);
        }).collect(Collectors.toList());
        return new MultiPolygon(lineStrings.stream().toArray(Polygon[]::new), geometryFactory);
    }

    public GeometryCollection geometryCollectionDeserialize(org.geojson.GeometryCollection geoJsonObjects) {
        List<Geometry> geometries = geoJsonObjects.getGeometries().stream().map(a -> {
            try {
                return geometryDeserialize(a);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        geometries.removeAll(Collections.singleton(null));
        Geometry[] geometries1 = geometries.stream().toArray(Geometry[]::new);
        return new GeometryCollection(geometries1, geometryFactory);
    }

    public Geometry geometryDeserialize(GeoJsonObject geometry) throws Exception {
        if (geometry.getClass() == org.geojson.Point.class) {
            return pointDeserialize((org.geojson.Point) geometry);
        } else if (geometry.getClass() == org.geojson.LineString.class) {
            return lineStringDeserialize((org.geojson.LineString) geometry);
        } else if (geometry.getClass() == org.geojson.Polygon.class) {
            return polygonDeserialize((org.geojson.Polygon) geometry);
        } else if (geometry.getClass() == org.geojson.MultiPoint.class) {
            return multiPointDeserialize((org.geojson.MultiPoint) geometry);
        } else if (geometry.getClass() == org.geojson.MultiLineString.class) {
            return multiLineStringDeserialize((org.geojson.MultiLineString) geometry);
        } else if (geometry.getClass() == org.geojson.MultiPolygon.class) {
            return multiPolygonDeserialize((org.geojson.MultiPolygon) geometry);
        } else if (geometry.getClass() == org.geojson.GeometryCollection.class) {
            return geometryCollectionDeserialize((org.geojson.GeometryCollection) geometry);
        } else {
            throw new Exception("不支持的几何类型");
        }
    }
}
