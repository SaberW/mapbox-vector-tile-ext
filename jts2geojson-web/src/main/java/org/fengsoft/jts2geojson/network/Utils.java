package org.fengsoft.jts2geojson.network;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;

/**
 * @Author JerFer
 * @Date 2018/8/28---14:07
 */
public class Utils {
    public boolean isIntersect(LineSegment line1, LineSegment line2) {
        Coordinate coordinate = line1.intersection(line2);
        return coordinate != null;
    }

    public boolean isSelefCross(LineString lineString) {
        if (lineString.getNumPoints()<4) return false;
        lineString.isSimple();
        lineString.isValid();
        return false;
    }
}
