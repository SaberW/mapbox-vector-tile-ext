package org.fengsoft.jts2geojson.network;

import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.LinkedList;

/**
 * @Author JerFer
 * @Date 2018/8/28---13:47
 */
public class Node {
    private Long id;
    private Point point;
    LinkedList<LineString> arcColections;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public LinkedList<LineString> getArcColections() {
        return arcColections;
    }

    public void setArcColections(LinkedList<LineString> arcColections) {
        this.arcColections = arcColections;
    }
}
