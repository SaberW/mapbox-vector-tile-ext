/* (c) 2015 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.vector;

import com.google.common.base.Stopwatch;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.MapProducerCapabilities;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.map.AbstractMapOutputFormat;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VectorTileMapOutputFormat extends AbstractMapOutputFormat {

    /**
     * A logger for this class.
     */
    private static final Logger LOGGER = Logging.getLogger(VectorTileMapOutputFormat.class);

    private final VectorTileBuilderFactory tileBuilderFactory;

    private boolean clipToMapBounds = true;

    private double overSamplingFactor = 2.0; // 1=no oversampling, 4=four time oversample (generialization will be 1/4 pixel)

    private boolean transformToScreenCoordinates = true;

    public VectorTileMapOutputFormat(VectorTileBuilderFactory tileBuilderFactory) {
        super(tileBuilderFactory.getMimeType(), tileBuilderFactory.getOutputFormats());
        this.tileBuilderFactory = tileBuilderFactory;
    }

    @Override
    public byte[] produceMap(final WMSMapContent mapContent, List<Layer> layers, CoordinateReferenceSystem sourceCrs) throws ServiceException, IOException {
        final ReferencedEnvelope renderingArea = mapContent.getRenderingArea();
        final Rectangle paintArea = new Rectangle(mapContent.getMapWidth(), mapContent.getMapHeight());

        VectorTileBuilder vectorTileBuilder;
        vectorTileBuilder = this.tileBuilderFactory.newBuilder(paintArea, renderingArea);

        double res = renderingArea.getWidth() * 3600.0 / 256.0;

        for (Layer layer : layers) {
            int buffer = mapContent.getBuffer();
            Pipeline pipeline = null;
            try {
                pipeline = getPipeline(renderingArea, paintArea, sourceCrs, buffer);
            } catch (FactoryException e) {
                e.printStackTrace();
            }
            FeatureCollection<?, ?> features = layer.getFeatureSource().getFeatures();

            run(features, pipeline, vectorTileBuilder, layer, res, renderingArea);
        }

        return vectorTileBuilder.build();
    }

    protected Pipeline getPipeline(final ReferencedEnvelope renderingArea, final Rectangle paintArea, CoordinateReferenceSystem sourceCrs, int buffer) throws FactoryException {
        Pipeline pipeline;
        try {
            final PipelineBuilder builder = PipelineBuilder.newBuilder(renderingArea, paintArea, sourceCrs, overSamplingFactor, buffer);
            pipeline = builder.preprocess().transform(transformToScreenCoordinates).simplify(transformToScreenCoordinates).clip(clipToMapBounds, transformToScreenCoordinates).collapseCollections().build();
        } catch (FactoryException e) {
            throw e;
        }
        return pipeline;
    }

    private Map<String, Object> getProperties(ComplexAttribute feature) {
        Map<String, Object> props = new TreeMap<>();
        for (Property p : feature.getProperties()) {
            if (!(p instanceof Attribute) || (p instanceof GeometryAttribute)) {
                continue;
            }
            String name = p.getName().getLocalPart();
            if (name.equalsIgnoreCase("shape")) continue;
            Object value;
            if (p instanceof ComplexAttribute) {
                value = getProperties((ComplexAttribute) p);
            } else {
                value = p.getValue();
            }
            if (value != null) {
                props.put(name, value);
            }
        }
        return props;
    }

    void run(FeatureCollection<?, ?> features, Pipeline pipeline, VectorTileBuilder vectorTileBuilder, Layer layer, double res, ReferencedEnvelope renderingArea) {
        Stopwatch sw = Stopwatch.createStarted();
        int count = 0;
        int total = 0;
        Feature feature;

        try (FeatureIterator<?> it = features.features()) {
            while (it.hasNext()) {
                feature = it.next();
                total++;
                Geometry originalGeom;
                Geometry finalGeom;
                originalGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();
                try {
                    if (originalGeom.getGeometryType().equalsIgnoreCase("Point"))
                        finalGeom = originalGeom.getFactory().createPoint(coordinateToScreeen(originalGeom.getCoordinate(), renderingArea, res));
                    else if (originalGeom.getGeometryType().equalsIgnoreCase("Polygon")) {
                        Polygon polygon = (Polygon) originalGeom;
                        Coordinate[] exterioRingArrs = Arrays.stream(polygon.getExteriorRing().getCoordinates()).map(coordinate -> coordinateToScreeen(coordinate, renderingArea, res)).toArray(Coordinate[]::new);
                        List<Coordinate[]> coordinates = new ArrayList<>();
                        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                            Coordinate[] interioRingArrs = Arrays.stream(polygon.getInteriorRingN(i).getCoordinates()).map(coordinate -> coordinateToScreeen(coordinate, renderingArea, res)).toArray(Coordinate[]::new);
                            coordinates.add(interioRingArrs);
                        }
                        finalGeom = originalGeom.getFactory().createPolygon(originalGeom.getFactory().createLinearRing(exterioRingArrs), coordinates.stream().map(coords -> originalGeom.getFactory().createLinearRing(coords)).toArray(LinearRing[]::new));
                    } else
                        finalGeom = pipeline.execute(originalGeom);
//                    公式：X = (lon - minLon)*3600/scaleX；
//                    公式：Y = (maxLat - lat)*3600/scaleY；
                } catch (Exception processingException) {
                    processingException.printStackTrace();
                    continue;
                }
                if (finalGeom.isEmpty()) {
                    continue;
                }

                final String layerName = feature.getName().getLocalPart();
                final String featureId = feature.getIdentifier().toString();
                final String geometryName = "shape";

                final Map<String, Object> properties = getProperties(feature);

                vectorTileBuilder.addFeature(layerName, featureId, geometryName, finalGeom, properties);
                count++;
            }
        }
        sw.stop();
        if (LOGGER.isLoggable(Level.FINE)) {
            String msg = String.format("Added %,d out of %,d features of '%s' in %s", count, total, layer.getTitle(), sw);
            LOGGER.fine(msg);
        }
    }

    private Coordinate coordinateToScreeen(Coordinate coordinate, ReferencedEnvelope renderingArea, double res) {
        return new Coordinate((coordinate.getX() - renderingArea.getMinX()) * 3600.0 / res, (renderingArea.getMaxY() - coordinate.getY()) * 3600.0 / res, 0);
    }

    /**
     * @return {@code null}, not a raster format.
     */
    @Override
    public MapProducerCapabilities getCapabilities(String format) {
        return null;
    }
}
