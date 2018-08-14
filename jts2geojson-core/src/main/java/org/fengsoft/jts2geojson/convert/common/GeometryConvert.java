package org.fengsoft.jts2geojson.convert.common;

import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.Arrays;
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
    public org.geojson.Point pointSerializable(Point point) {
        return new org.geojson.Point(point.getX(), point.getY(), 0);
    }

    /**
     * 线对象转换成JSON lineString
     *
     * @param lineString
     * @return
     */
    public org.geojson.LineString lineStringSerializable(LineString lineString) {
        List<LngLatAlt> lngLatAltList = coordinatesToLngLatAltsList(lineString.getCoordinates());
        LngLatAlt[] coords = new LngLatAlt[lngLatAltList.size()];
        lngLatAltList.toArray(coords);
        return new org.geojson.LineString(coords);
    }

    /**
     * 线对象转换成JSON polygon
     *
     * @param polygon
     * @return
     */
    public org.geojson.Polygon polygonSerializable(Polygon polygon) {
        org.geojson.Polygon polygonOut = new org.geojson.Polygon();
        int ringCount = polygon.getNumInteriorRing();
        for (int i = 0; i < ringCount; i++) {
            List<LngLatAlt> lngLatAltList = coordinatesToLngLatAltsList(polygon.getInteriorRingN(i).getCoordinates());
            LngLatAlt[] coords = new LngLatAlt[lngLatAltList.size()];
            lngLatAltList.toArray(coords);
            polygonOut.addInteriorRing(coords);
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
    public org.geojson.MultiPoint multiPointSerializable(MultiPoint multiPoint) {
        List<LngLatAlt> lngLatAlts = coordinatesToLngLatAltsList(multiPoint.getCoordinates());
        LngLatAlt[] coords = new LngLatAlt[lngLatAlts.size()];
        lngLatAlts.toArray(coords);
        return new org.geojson.MultiPoint(coords);
    }

    /**
     * 多线对象转换成JSON multiLineString
     *
     * @param multiLineString
     * @return
     */
    public org.geojson.MultiLineString multiLineStringSerializable(MultiLineString multiLineString) {
        return new org.geojson.MultiLineString(coordinatesToLngLatAltsList(multiLineString.getCoordinates()));
    }

    /**
     * 多面对象转换成JSON multiPolygon(功能待完善)
     *
     * @param multiPolygon
     * @return
     */
    public org.geojson.MultiPolygon multiPolygonSerializable(MultiPolygon multiPolygon) {
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

    public GeoJsonObject geometrySerializer(Geometry geometry) throws Exception {
        if (geometry instanceof Point) {
            return pointSerializable((Point) geometry);
        } else if (geometry instanceof LineString) {
            return lineStringSerializable((LineString) geometry);
        } else if (geometry instanceof Polygon) {
            return polygonSerializable((Polygon) geometry);
        } else if (geometry instanceof MultiPoint) {
            return multiPointSerializable((MultiPoint) geometry);
        } else if (geometry instanceof MultiLineString) {
            return multiLineStringSerializable((MultiLineString) geometry);
        } else if (geometry instanceof MultiPolygon) {
            return multiPolygonSerializable((MultiPolygon) geometry);
        } else {
            throw new Exception("非法的输入数据");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Coordinate[] lngLatAltToCoordinates(List<LngLatAlt> coordinates) {
        return (Coordinate[]) coordinates.stream().map(a -> new Coordinate(a.getLongitude(), a.getLatitude())).toArray();
    }

    public Point pointDeserialize(org.geojson.Point point) {
        return new Point(new Coordinate(point.getCoordinates().getLongitude(), point.getCoordinates().getLatitude()), geometryFactory.getPrecisionModel(), geometryFactory.getSRID());
    }

    public LineString lineStringDeserialize(org.geojson.LineString lineString) {
        Coordinate[] coordinates = lngLatAltToCoordinates(lineString.getCoordinates());
        return new LineString(coordinates, geometryFactory.getPrecisionModel(), geometryFactory.getSRID());
    }

    public Polygon polygonDeserialize(org.geojson.Polygon polygon) {
        LinearRing linearRing = new LinearRing(new CoordinateArraySequence(lngLatAltToCoordinates(polygon.getExteriorRing())), geometryFactory);
        List<List<LngLatAlt>> coordinates1 = polygon.getInteriorRings();
        LinearRing[] linearRings = (LinearRing[]) coordinates1.stream().map(item -> new LinearRing(new CoordinateArraySequence(lngLatAltToCoordinates(item)), geometryFactory)).toArray();
        return new Polygon(linearRing, linearRings, geometryFactory);
    }

    public MultiPoint multiPointDeserialize(org.geojson.MultiPoint multiPoint) {
        Point[] points = (Point[]) multiPoint.getCoordinates().stream().map(item -> pointDeserialize(new org.geojson.Point(item))).toArray();
        return new MultiPoint(points, geometryFactory);
    }

    public MultiLineString multiLineStringDeserialize(org.geojson.MultiLineString multiLineString) {
        List<LineString> lineStrings = multiLineString.getCoordinates().stream().map(item -> {
            return lineStringDeserialize(new org.geojson.LineString((LngLatAlt[]) item.toArray()));
        }).collect(Collectors.toList());
        return new MultiLineString((LineString[]) lineStrings.toArray(), geometryFactory);
    }

    public MultiPolygon multiPolygonDeserialize(org.geojson.MultiPolygon multiPolygon) {
        List<Polygon> lineStrings = multiPolygon.getCoordinates().stream().map(item -> {
            return polygonDeserialize(new org.geojson.Polygon((LngLatAlt[]) item.toArray()));
        }).collect(Collectors.toList());
        return new MultiPolygon((Polygon[]) lineStrings.toArray(), geometryFactory);
    }

    public Geometry geometryDeserialize(GeoJsonObject geometry) throws Exception {
        if (geometry instanceof org.geojson.Point) {
            return pointDeserialize((org.geojson.Point) geometry);
        } else if (geometry instanceof org.geojson.LineString) {
            return lineStringDeserialize((org.geojson.LineString) geometry);
        } else if (geometry instanceof org.geojson.Polygon) {
            return polygonDeserialize((org.geojson.Polygon) geometry);
        } else if (geometry instanceof org.geojson.MultiPoint) {
            return multiPointDeserialize((org.geojson.MultiPoint) geometry);
        } else if (geometry instanceof org.geojson.MultiLineString) {
            return multiLineStringDeserialize((org.geojson.MultiLineString) geometry);
        } else if (geometry instanceof org.geojson.MultiPolygon) {
            return multiPolygonDeserialize((org.geojson.MultiPolygon) geometry);
        } else {
            throw new Exception("不支持的几何类型");
        }
    }
}
