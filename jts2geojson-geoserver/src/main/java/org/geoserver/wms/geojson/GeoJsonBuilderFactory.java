/* (c) 2015 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.geojson;

import com.google.common.collect.ImmutableSet;
import java.awt.Rectangle;
import java.util.Set;
import org.geoserver.wms.vector.VectorTileBuilderFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class GeoJsonBuilderFactory implements VectorTileBuilderFactory {
    public static final String MIME_TYPE = "application/json;type=geojson";
    public static final Set<String> OUTPUT_FORMATS = ImmutableSet.of("application/json;type=geojson", "geojson");

    public GeoJsonBuilderFactory() {
    }

    public Set<String> getOutputFormats() {
        return OUTPUT_FORMATS;
    }

    public String getMimeType() {
        return "application/json;type=geojson";
    }

    public GeoJsonWMSBuilder newBuilder(Rectangle screenSize, ReferencedEnvelope mapArea) {
        return new GeoJsonWMSBuilder(screenSize, mapArea);
    }
}
