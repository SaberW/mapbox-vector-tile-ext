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
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.renderer.lite.VectorMapRenderUtils;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.*;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotools.renderer.lite.VectorMapRenderUtils.getStyleQuery;

public class VectorTileMapOutputFormat extends AbstractMapOutputFormat {

    /**
     * A logger for this class.
     */
    private static final Logger LOGGER = Logging.getLogger(VectorTileMapOutputFormat.class);

    private final VectorTileBuilderFactory tileBuilderFactory;

    private boolean clipToMapBounds;

    private double overSamplingFactor = 2.0; // 1=no oversampling, 4=four time oversample (generialization will be 1/4 pixel)

    private boolean transformToScreenCoordinates;

    public VectorTileMapOutputFormat(VectorTileBuilderFactory tileBuilderFactory) {
        super(tileBuilderFactory.getMimeType(), tileBuilderFactory.getOutputFormats());
        this.tileBuilderFactory = tileBuilderFactory;
    }

    /**
     * Multiplies density of simplification from its base value.
     *
     * @param factor
     */
    public void setOverSamplingFactor(double factor) {
        this.overSamplingFactor = factor;
    }

    /**
     * Does this format use features clipped to the extent of the tile instead of whole features
     *
     * @param clip
     */
    public void setClipToMapBounds(boolean clip) {
        this.clipToMapBounds = clip;
    }

    /**
     * Does this format use screen coordinates
     */
    public void setTransformToScreenCoordinates(boolean useScreenCoords) {
        this.transformToScreenCoordinates = useScreenCoords;
    }

    @Override
    public byte[] produceMap(final WMSMapContent mapContent, CoordinateReferenceSystem coordinateReferenceSystem) throws ServiceException, IOException {
        final ReferencedEnvelope renderingArea = mapContent.getRenderingArea();
        final Rectangle paintArea = new Rectangle(mapContent.getMapWidth(), mapContent.getMapHeight());

        VectorTileBuilder vectorTileBuilder;
        vectorTileBuilder = this.tileBuilderFactory.newBuilder(paintArea, renderingArea);

        CoordinateReferenceSystem sourceCrs;
        for (Layer layer : mapContent.layers()) {
            FeatureSource<?, ?> featureSource = layer.getFeatureSource();
            GeometryDescriptor geometryDescriptor = featureSource.getSchema().getGeometryDescriptor();
            if (null == geometryDescriptor) {
                continue;
            }

            sourceCrs = geometryDescriptor.getType().getCoordinateReferenceSystem();
            int buffer = VectorMapRenderUtils.getComputedBuffer(mapContent.getBuffer(), VectorMapRenderUtils.getFeatureStyles(layer, paintArea, VectorMapRenderUtils.getMapScale(mapContent, renderingArea), featureSource.getSchema()));
            Pipeline pipeline = getPipeline(mapContent, renderingArea, paintArea, sourceCrs, buffer);

            Query query = getStyleQuery(layer, mapContent);
            query.getHints().remove(Hints.SCREENMAP);
            FeatureCollection<?, ?> features = featureSource.getFeatures(query);
            run(layer.getFeatureSource().getFeatures(), pipeline, geometryDescriptor, vectorTileBuilder, layer);
        }
        return vectorTileBuilder.build();
    }

    protected Pipeline getPipeline(final WMSMapContent mapContent, final ReferencedEnvelope renderingArea, final Rectangle paintArea, CoordinateReferenceSystem sourceCrs, int buffer) {
        Pipeline pipeline;
        try {
            final PipelineBuilder builder = PipelineBuilder.newBuilder(renderingArea, paintArea, sourceCrs, overSamplingFactor, buffer);
            pipeline = builder.preprocess().transform(transformToScreenCoordinates).simplify(transformToScreenCoordinates).clip(clipToMapBounds, transformToScreenCoordinates).collapseCollections().build();
        } catch (FactoryException e) {
            throw new ServiceException(e);
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

    void run(FeatureCollection<?, ?> features, Pipeline pipeline, GeometryDescriptor geometryDescriptor, VectorTileBuilder vectorTileBuilder, Layer layer) {
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
                    finalGeom = pipeline.execute(originalGeom);
                } catch (Exception processingException) {
                    processingException.printStackTrace();
                    continue;
                }
                if (finalGeom.isEmpty()) {
                    continue;
                }

                final String layerName = feature.getName().getLocalPart();
                final String featureId = feature.getIdentifier().toString();
                final String geometryName = geometryDescriptor.getName().getLocalPart();

                final Map<String, Object> properties = getProperties(feature);
                properties.put("shape", null);

                vectorTileBuilder.addFeature(layerName, featureId, geometryName, finalGeom, properties);
                count++;
            }
        }
        sw.stop();
        if (LOGGER.isLoggable(Level.FINE)) {
            String msg = String.format("Added %,d out of %,d features of '%s' in %s", count, total, layer.getTitle(), sw);
            // System.err.println(msg);
            LOGGER.fine(msg);
        }
    }

    /**
     * @return {@code null}, not a raster format.
     */
    @Override
    public MapProducerCapabilities getCapabilities(String format) {
        return null;
    }
}
