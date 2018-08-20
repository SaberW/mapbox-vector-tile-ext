package org.fengsoft.jts2geojson.convert.common;

import no.ecc.vectortile.VectorTileEncoder;
import org.locationtech.jts.geom.Geometry;

/**
 * @Author JerFer
 * @Date 2018/8/20---9:53
 */
public class VectorTileEncoderNoClip extends VectorTileEncoder {
    public VectorTileEncoderNoClip(int extent, int polygonClipBuffer, boolean autoScale) {
        super(extent, polygonClipBuffer, autoScale);
    }

    /*
     * returns original geometry - no clipping. Assume upstream has already clipped!
     */
    protected Geometry clipGeometry(Geometry geometry) {
        return geometry;
    }
}
