package org.fengsoft.jts2geojson.network;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/**
 * @Author JerFer
 * @Date 2018/8/28---13:50
 */
public class Arc {
    private Long id;
    private LineString lineString;
    Node start;
    Node end;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LineString getLineString() {
        return lineString;
    }

    public void setLineString(LineString lineString) {
        this.lineString = lineString;
    }

    public Node getStart() {
        return start;
    }

    public void setStart(Node start) {
        this.start = start;
    }

    public Node getEnd() {
        return end;
    }

    public void setEnd(Node end) {
        this.end = end;
    }
}
