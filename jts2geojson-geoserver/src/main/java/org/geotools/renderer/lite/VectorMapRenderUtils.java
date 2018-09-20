/* (c) 2015 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geotools.renderer.lite;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.geoserver.wms.WMSMapContent;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureTypes;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.spatial.DefaultCRSFilterVisitor;
import org.geotools.filter.spatial.ReprojectingFilterVisitor;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.filter.visitor.SpatialFilterVisitor;
import org.geotools.geometry.jts.Decimator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

public class VectorMapRenderUtils {
    private static final Logger LOGGER = Logging.getLogger(VectorMapRenderUtils.class);
    private static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();

    public VectorMapRenderUtils() {
    }

    public static Query getStyleQuery(Layer layer, WMSMapContent mapContent) throws IOException {
        ReferencedEnvelope renderingArea = mapContent.getRenderingArea();
        Rectangle screenSize = new Rectangle(mapContent.getMapWidth(), mapContent.getMapHeight());
        double mapScale = getMapScale(mapContent, renderingArea);
        int requestBufferScreen = mapContent.getBuffer();
        double[] pixelSize = getPixelSize(renderingArea, screenSize);
        FeatureSource<?, ?> featureSource = layer.getFeatureSource();
        FeatureType schema = featureSource.getSchema();
        List<LiteFeatureTypeStyle> styleList = getFeatureStyles(layer, screenSize, mapScale, schema);
        if (styleList.isEmpty()) {
            Query query = new Query(schema.getName().getLocalPart());
            query.setProperties(Query.NO_PROPERTIES);
            query.setFilter(Filter.EXCLUDE);
            return query;
        } else {
            int bufferScreen = getComputedBuffer(requestBufferScreen, styleList);
            ReferencedEnvelope queryArea = new ReferencedEnvelope(renderingArea);
            queryArea.expandBy((double)bufferScreen * Math.max(pixelSize[0], pixelSize[1]));
            GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();

            Query styleQuery;
            try {
                styleQuery = getStyleQuery(featureSource, styleList, queryArea, screenSize, geometryDescriptor);
            } catch (FactoryException | IllegalFilterException var17) {
                throw Throwables.propagate(var17);
            }

            Query query = DataUtilities.mixQueries(styleQuery, layer.getQuery(), null);
            query.setProperties(Query.ALL_PROPERTIES);
            Hints hints = query.getHints();
            hints.put(Hints.FEATURE_2D, Boolean.TRUE);
            return query;
        }
    }

    public static double getMapScale(WMSMapContent mapContent, ReferencedEnvelope renderingArea) {
        try {
            double mapScale = RendererUtilities.calculateScale(renderingArea, mapContent.getMapWidth(), mapContent.getMapHeight(), (Map)null);
            return mapScale;
        } catch (FactoryException | TransformException var5) {
            throw Throwables.propagate(var5);
        }
    }

    public static int getComputedBuffer(int requestBufferScreen, List<LiteFeatureTypeStyle> styleList) {
        int bufferScreen;
        if (requestBufferScreen <= 0) {
            MetaBufferEstimator bufferEstimator = new MetaBufferEstimator();
            styleList.stream().flatMap((fts) -> Stream.concat(Arrays.stream(fts.elseRules), Arrays.stream(fts.ruleList))).forEach(bufferEstimator::visit);
            bufferScreen = bufferEstimator.getBuffer();
        } else {
            bufferScreen = requestBufferScreen;
        }

        return bufferScreen;
    }

    public static List<LiteFeatureTypeStyle> getFeatureStyles(Layer layer, Rectangle screenSize, double mapScale, FeatureType schema) throws IOException {
        Style style = layer.getStyle();
        List<FeatureTypeStyle> featureStyles = style.featureTypeStyles();
        List<LiteFeatureTypeStyle> styleList = createLiteFeatureTypeStyles(layer, featureStyles, schema, mapScale, screenSize);
        return styleList;
    }

    protected static double[] getPixelSize(ReferencedEnvelope renderingArea, Rectangle screenSize) {
        double[] pixelSize;
        try {
            pixelSize = Decimator.computeGeneralizationDistances(ProjectiveTransform.create(RendererUtilities.worldToScreenTransform(renderingArea, screenSize)).inverse(), screenSize, 1.0D);
        } catch (TransformException var4) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Error while computing pixel size", var4);
            }

            pixelSize = new double[]{renderingArea.getWidth() / screenSize.getWidth(), renderingArea.getHeight() / screenSize.getHeight()};
        }

        return pixelSize;
    }

    private static Query getStyleQuery(FeatureSource<?, ?> source, List<LiteFeatureTypeStyle> styleList, ReferencedEnvelope queryArea, Rectangle screenSize, GeometryDescriptor geometryAttribute) throws IllegalFilterException, IOException, FactoryException {
        FeatureType schema = source.getSchema();
        Query query = new Query(schema.getName().getLocalPart());
        query.setProperties(Query.ALL_PROPERTIES);
        String geomName = geometryAttribute.getLocalName();
        Filter filter = reprojectSpatialFilter(queryArea.getCoordinateReferenceSystem(), schema, FF.bbox(FF.property(geomName), queryArea));
        query.setFilter(filter);
        LiteFeatureTypeStyle[] styles = styleList.toArray(new LiteFeatureTypeStyle[styleList.size()]);

        try {
            processRuleForQuery(styles, query);
        } catch (Exception var12) {
            throw Throwables.propagate(var12);
        }

        SimplifyingFilterVisitor simplifier = new SimplifyingFilterVisitor();
        simplifier.setFeatureType(source.getSchema());
        Filter simplifiedFilter = (Filter)query.getFilter().accept(simplifier, null);
        query.setFilter(simplifiedFilter);
        return query;
    }

    private static Filter reprojectSpatialFilter(CoordinateReferenceSystem declaredCRS, FeatureType schema, Filter filter) {
        if (filter == null) {
            return null;
        } else {
            SpatialFilterVisitor sfv = new SpatialFilterVisitor();
            filter.accept(sfv, null);
            if (!sfv.hasSpatialFilter()) {
                return filter;
            } else {
                DefaultCRSFilterVisitor defaulter = new DefaultCRSFilterVisitor(FF, declaredCRS);
                Filter defaulted = (Filter)filter.accept(defaulter, null);
                ReprojectingFilterVisitor reprojector = new ReprojectingFilterVisitor(FF, schema);
                Filter reprojected = (Filter)defaulted.accept(reprojector, null);
                return reprojected;
            }
        }
    }

    public static MathTransform buildTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem destCRS) throws FactoryException {
        Preconditions.checkNotNull(sourceCRS, "sourceCRS");
        Preconditions.checkNotNull(destCRS, "destCRS");
        MathTransform transform = null;
        if (sourceCRS.getCoordinateSystem().getDimension() >= 3) {
            MathTransform toWgs84_3d = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84_3D);
            MathTransform toWgs84_2d = CRS.findMathTransform(DefaultGeographicCRS.WGS84_3D, DefaultGeographicCRS.WGS84);
            transform = ConcatenatedTransform.create(toWgs84_3d, toWgs84_2d);
            sourceCRS = DefaultGeographicCRS.WGS84;
        }

        MathTransform2D sourceToTarget = (MathTransform2D)CRS.findMathTransform(sourceCRS, destCRS, true);
        if (transform == null) {
            return sourceToTarget;
        } else {
            return sourceToTarget.isIdentity() ? transform : ConcatenatedTransform.create(transform, sourceToTarget);
        }
    }

    private static void processRuleForQuery(LiteFeatureTypeStyle[] styles, Query query) {
        try {
//            int maxFilters = true;
            List<Filter> filtersToDS = new ArrayList();
            LiteFeatureTypeStyle[] var4 = styles;
            int var5 = styles.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                LiteFeatureTypeStyle style = var4[var6];
                if (style.elseRules.length > 0) {
                    return;
                }

                Rule[] var8 = style.ruleList;
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    Rule r = var8[var10];
                    if (r.getFilter() == null) {
                        return;
                    }

                    filtersToDS.add(r.getFilter());
                }
            }

            if (filtersToDS.size() > 5) {
                return;
            }

            Object ruleFiltersCombined;
            if (filtersToDS.size() == 1) {
                ruleFiltersCombined = filtersToDS.get(0);
            } else {
                ruleFiltersCombined = FF.or(filtersToDS);
            }

            Filter filter = FF.and(query.getFilter(), (Filter) ruleFiltersCombined);
            query.setFilter(filter);
        } catch (Exception var12) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.SEVERE, "Could not send rules to datastore due to: " + var12.getMessage(), var12);
            }
        }

    }

    static ArrayList<LiteFeatureTypeStyle> createLiteFeatureTypeStyles(Layer layer, List<FeatureTypeStyle> featureStyles, FeatureType ftype, double scaleDenominator, Rectangle screenSize) throws IOException {
        ArrayList<LiteFeatureTypeStyle> result = new ArrayList();
        Iterator var8 = featureStyles.iterator();

        while(true) {
            FeatureTypeStyle fts;
            List ruleList;
            List elseRuleList;
            do {
                do {
                    if (!var8.hasNext()) {
                        return result;
                    }

                    fts = (FeatureTypeStyle)var8.next();
                } while(!isFeatureTypeStyleActive(ftype, fts));

                List<Rule>[] splittedRules = splitRules(fts, scaleDenominator);
                ruleList = splittedRules[0];
                elseRuleList = splittedRules[1];
            } while(ruleList.isEmpty() && elseRuleList.isEmpty());

            Graphics2D graphics = null;
            LiteFeatureTypeStyle lfts = new LiteFeatureTypeStyle(layer, graphics, ruleList, elseRuleList, fts.getTransformation());
            result.add(lfts);
        }
    }

    private static List<Rule>[] splitRules(FeatureTypeStyle fts, double scaleDenominator) {
        new ArrayList();
        new ArrayList();
        List<Rule> ruleList = new ArrayList();
        List<Rule> elseRuleList = new ArrayList();
        Iterator var5 = fts.rules().iterator();

        while(var5.hasNext()) {
            Rule r = (Rule)var5.next();
            if (isWithInScale(r, scaleDenominator)) {
                if (r.isElseFilter()) {
                    elseRuleList.add(r);
                } else {
                    ruleList.add(r);
                }
            }
        }

        List<Rule>[] ret = new List[]{ruleList, elseRuleList};
        return ret;
    }

    private static boolean isWithInScale(Rule r, double scaleDenominator) {
        double TOLERANCE = 1.0E-6D;
        return r.getMinScaleDenominator() - 1.0E-6D <= scaleDenominator && r.getMaxScaleDenominator() + 1.0E-6D > scaleDenominator;
    }

    private static boolean isFeatureTypeStyleActive(FeatureType ftype, FeatureTypeStyle fts) {
        return fts.featureTypeNames().isEmpty() || ftype.getName().getLocalPart() != null && (ftype.getName().getLocalPart().equalsIgnoreCase(fts.getFeatureTypeName()) || FeatureTypes.isDecendedFrom(ftype, (URI)null, fts.getFeatureTypeName()));
    }
}
