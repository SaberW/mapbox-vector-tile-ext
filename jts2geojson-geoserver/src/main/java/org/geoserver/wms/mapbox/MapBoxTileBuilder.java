/* (c) 2015 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.mapbox;

import java.awt.Rectangle;
import java.util.Map;
import no.ecc.vectortile.VectorTileEncoder;
import no.ecc.vectortile.VectorTileEncoderNoClip;
import org.geoserver.wms.vector.VectorTileBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;

/**
 * @author Niels Charlier
 */
public class MapBoxTileBuilder implements VectorTileBuilder {

    private VectorTileEncoder encoder;

    public MapBoxTileBuilder(Rectangle mapSize, ReferencedEnvelope mapArea) {
        final int extent = Math.max(mapSize.width, mapSize.height);
        final int polygonClipBuffer = extent / 32;
        final boolean autoScale = false;
        this.encoder = new VectorTileEncoderNoClip(extent, polygonClipBuffer, autoScale);
    }

    @Override
    public void addFeature(String layerName, String featureId, String geometryName, Geometry geometry, Map<String, Object> properties) {
        encoder.addFeature(layerName, properties, geometry);
    }

    @Override
    public byte[] build() {
        return encoder.encode();
    }
}
