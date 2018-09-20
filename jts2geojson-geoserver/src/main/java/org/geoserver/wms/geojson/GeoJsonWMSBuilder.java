/* (c) 2015 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.geojson;

import com.google.common.base.Charsets;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import javax.measure.Unit;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.geoserver.wms.vector.VectorTileBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.CoordinatePrecisionReducerFilter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import si.uom.SI;

public class GeoJsonWMSBuilder implements VectorTileBuilder {

    private Writer writer;

    private CoordinatePrecisionReducerFilter precisionReducerFilter;

    private DeferredFileOutputStream out;

    private JsonWriter jsonWriter;
//    private org.geoserver.wfs.json.GeoJSONBuilder jsonWriter;


    public GeoJsonWMSBuilder(Rectangle mapSize, ReferencedEnvelope mapArea) {

        final int memotyBufferThreshold = 8096;
        out = new DeferredFileOutputStream(memotyBufferThreshold, "geojson", ".geojson", null);
        writer = new OutputStreamWriter(out, Charsets.UTF_8);

        jsonWriter = new JsonWriter(writer);

//        jsonWriter = new org.geoserver.wfs.json.GeoJSONBuilder(writer);
//        jsonWriter.object(); // start root object
//        jsonWriter.key("type").value("FeatureCollection");
//        jsonWriter.key("totalFeatures").value("unknown");
//        jsonWriter.key("features");
//        jsonWriter.array();
        try {
            jsonWriter.beginObject();
            jsonWriter.name("type").value("FeatureCollection").name("totalFeatures").value("unknown").name("features").beginArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        CoordinateReferenceSystem mapCrs = mapArea.getCoordinateReferenceSystem();
        //jsonWriter.setAxisOrder(CRS.getAxisOrder(mapCrs));

        Unit<?> unit = mapCrs.getCoordinateSystem().getAxis(0).getUnit();
        Unit<?> standardUnit = unit.getSystemUnit();

        PrecisionModel pm = null;
        if (SI.RADIAN.equals(standardUnit)) {
            pm = new PrecisionModel(1e6); // truncate coords at 6 decimals
        } else if (SI.METRE.equals(standardUnit)) {
            pm = new PrecisionModel(100); // truncate coords at 2 decimals
        }
        if (pm != null) {
            precisionReducerFilter = new CoordinatePrecisionReducerFilter(pm);
        }
    }

    @Override
    public void addFeature(
            String layerName,
            String featureId,
            String geometryName,
            Geometry aGeom,
            Map<String, Object> properties) {

        if (precisionReducerFilter != null) {
            aGeom.apply(precisionReducerFilter);
        }


//        jsonWriter.object();
//        jsonWriter.key("type").value("Feature");
//
//        jsonWriter.key("id").value(featureId);
//
//        jsonWriter.key("geometry");
//
//        // Write the geometry, whether it is a null or not
//        jsonWriter.writeGeom(aGeom);
//        jsonWriter.key("geometry_name").value(geometryName);
//
//        jsonWriter.key("properties");
//        jsonWriter.object();
        try {
            jsonWriter.beginObject().name("type").value("Feature").name("id").value(featureId).name("geometry").value("");
            jsonWriter.name("geometry_name").value(geometryName).name("properties").beginObject();

            for (Map.Entry<String, Object> e : properties.entrySet()) {
                String attributeName = e.getKey();
                Object value = e.getValue();

                jsonWriter.name(attributeName);
                if (value == null) {
                    jsonWriter.nullValue();
                } else {
                    jsonWriter.value(value.toString());
                }
            }

            jsonWriter.endObject(); // end the properties
            jsonWriter.endObject(); // end the feature
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public byte[] build() throws IOException {
        jsonWriter.endArray(); // end features
        jsonWriter.endObject(); // end root object
        writer.flush();
        writer.close();
        out.close();

        return out.getData();
    }
}
