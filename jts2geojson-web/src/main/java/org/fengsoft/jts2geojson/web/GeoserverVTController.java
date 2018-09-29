package org.fengsoft.jts2geojson.web;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLReady;
import org.fengsoft.geojson.common.GeoEntity;
import org.fengsoft.geojson.common.GlobalGeodetic;
import org.fengsoft.geojson.common.GlobalMercator;
import org.fengsoft.geojson.entity.RegionCounty;
import org.fengsoft.jts2geojson.common.Underline2Camel;
import org.geoserver.wms.GetMapOutputFormat;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.mapbox.MapBoxTileBuilderFactory;
import org.geoserver.wms.vector.VectorTileMapOutputFormat;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.styling.StyleImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.postgresql.util.PGobject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "geoserver")
public class GeoserverVTController {
    @Value("${cache.vector-tile-geoserver-path}")
    public String cachePath;

    private GlobalGeodetic globalGeodetic = new GlobalGeodetic("", 256);
    private GlobalMercator globalMercator = new GlobalMercator(256);

    @Autowired
    @Qualifier("sqlManagerFactoryBeanPG")
    private SQLManager sqlManager;

    /**
     * 进来的是XYZ scheme
     *
     * @param layerName
     * @param x
     * @param y
     * @param z
     * @return
     */
    @RequestMapping(value = "vt/{z}/{x}/{y}.mvt", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String getLine2(@RequestParam("layerName") String layerName, @PathVariable("x") Integer x, @PathVariable("y") Integer y, @PathVariable("z") Integer z) {
        File parentFile = new File(cachePath + File.separator + layerName);
        if (!parentFile.exists()) parentFile.mkdir();

        File file = new File(cachePath + File.separator + layerName, String.format("%d-%d-%d", z, x, y) + ".mvt");
        if (!file.exists()) {
            double[] bboxs = globalGeodetic.tileLatLonBounds(x, y, z);
            CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
            ReferencedEnvelope envelope = new ReferencedEnvelope(bboxs[1], bboxs[3], bboxs[0], bboxs[2], crs);
            GetMapOutputFormat format = new VectorTileMapOutputFormat(new MapBoxTileBuilderFactory());
            WMSMapContent mapContent = new WMSMapContent(256, 256, 256, 0, 32);
            mapContent.setViewport(new MapViewport(envelope));

            List<FeatureLayer> layers = buildFeatureLayers(layerName, bboxs, crs);
            layers.forEach(lyr -> mapContent.addLayer(lyr));

            ((VectorTileMapOutputFormat) format).setClipToMapBounds(false);
            ((VectorTileMapOutputFormat) format).setTransformToScreenCoordinates(true);

            try {
                byte[] res = format.produceMap(mapContent, crs);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(res, 0, res.length);
                fos.flush();
                fos.close();
                mapContent.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "forward:/geoserver/download/" + layerName + "/" + file.getName();
    }

    private List<FeatureLayer> buildFeatureLayers(String layerName, double[] bboxs, CoordinateReferenceSystem crs) {
        List<FeatureLayer> featureLayers = new ArrayList<>();

        List<String> lyrNames = new ArrayList<>();
        if (layerName.equalsIgnoreCase("baseLayer")) {
            lyrNames.add("poi_village");
            lyrNames.add("water_line");
            lyrNames.add("region_county");
        } else lyrNames.add("layerName");

        lyrNames.forEach(lyr -> {
            String sql = "SELECT t.* FROM " + lyr + " t  WHERE ST_Intersects (shape,ST_MakeEnvelope(" + bboxs[1] + "," + bboxs[0] + "," + bboxs[3] + "," + bboxs[2] + ",4326))";
            String className = Underline2Camel.underline2Camel(lyr, false);

            try {
                Class clazz = Class.forName("org.fengsoft.geojson.entity." + className);
                List<GeoEntity> entityList = sqlManager.execute(new SQLReady(sql), clazz);

                if (entityList.size() > 0) {
                    List<AttributeDescriptor> attributeDescriptors = new ArrayList<>();
                    AtomicReference<SimpleFeatureTypeImpl> featureType = new AtomicReference<>();
                    AtomicReference<GeometryDescriptor> geometryDescriptor = new AtomicReference<>();
                    List<Field> fields = new ArrayList<>();
                    ReflectionUtils.doWithFields(RegionCounty.class, field -> {
                        AttributeType type = null;
                        if (field.getName().equalsIgnoreCase("id")) {
                            type = new AttributeTypeImpl(new NameImpl(field.getName()), field.getType(), true, false, null, null, null);
                        } else if (field.getName().equalsIgnoreCase("shape")) {
                            type = new GeometryTypeImpl(new NameImpl("shape"), Geometry.class, crs, false, false, null, null, null);
                            geometryDescriptor.set(new GeometryDescriptorImpl((GeometryType) type, new NameImpl("shape"), 0, 0, false, null));
                        } else {
                            type = new AttributeTypeImpl(new NameImpl(field.getName()), field.getType(), false, false, null, null, null);
                        }
                        attributeDescriptors.add(new AttributeDescriptorImpl(type, new NameImpl(field.getName()), 0, 0, false, null));

                        fields.add(field);
                    });
                    featureType.set(new SimpleFeatureTypeImpl(new NameImpl("feature"), attributeDescriptors, geometryDescriptor.get(), false, null, null, null));

                    SimpleFeatureCollection featureCollection = new ListFeatureCollection(featureType.get());
                    entityList.forEach(entity -> {
                        SimpleFeature feature = new SimpleFeatureImpl(transBean2Map(entity, fields), featureType.get(), new FeatureIdImpl(String.valueOf(entity.getId())));
                        ((ListFeatureCollection) featureCollection).add(feature);
                    });

                    StyleFactory styleFactory = new StyleFactoryImpl();
                    Style style = styleFactory.createStyle();
                    featureLayers.add(new FeatureLayer(featureCollection, style, lyr));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });


        return featureLayers;
    }

    @RequestMapping(value = "download/{layerName}/{fileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable(value = "layerName") String layerName, @PathVariable(value = "fileName") String fileName) throws IOException {
        String filePath = cachePath + File.separator + layerName + File.separator + fileName;
        if (new File(filePath).exists()) {
            FileSystemResource file = new FileSystemResource(filePath);
            return ResponseEntity.ok().contentLength(file.contentLength()).contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE)).body(new InputStreamResource(file.getInputStream()));
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    public static List<Object> transBean2Map(Object obj, List<Field> fields) {
        if (obj == null) return null;
        return fields.stream().map(field -> {
            Object value = null;
            try {
                Method getter = obj.getClass().getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
                value = getter.invoke(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (field.getName().equalsIgnoreCase("shape")) {
                if (value instanceof PGobject) {
                    PGobject pGobject = (PGobject) value;
                    try {
                        value = new WKBReader().read(WKBReader.hexToBytes(pGobject.getValue()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            return value;
        }).collect(Collectors.toList());
    }
}
