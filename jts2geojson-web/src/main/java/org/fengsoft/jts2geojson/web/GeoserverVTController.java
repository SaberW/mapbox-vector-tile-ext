package org.fengsoft.jts2geojson.web;

import cn.com.enersun.dgpmicro.common.GlobalGeodetic;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLReady;
import org.fengsoft.jts2geojson.entity.RegionCounty;
import org.geoserver.wms.GetMapOutputFormat;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.mapbox.MapBoxTileBuilderFactory;
import org.geoserver.wms.vector.VectorTileMapOutputFormat;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollections;
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
import org.geotools.map.Layer;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

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
    @Value("${cache.vector-tile-path}")
    public String cachePath;

    private GlobalGeodetic globalGeodetic = new GlobalGeodetic("", 256);

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
    @RequestMapping(value = "vt/{z}/{x}/{y}.mvt",
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public String getLine2(@RequestParam("layerName") String layerName,
                           @PathVariable("x") Integer x,
                           @PathVariable("y") Integer y,
                           @PathVariable("z") Integer z) {
        File parentFile = new File(cachePath + File.separator + layerName);
        if (!parentFile.exists()) parentFile.mkdir();

        //y = (int) Math.pow(2, z) - 1 - y;// TMS转XYZ
        //y = (1 << z) - y - 1;            //将XYZ 转为 TMS

        File file = new File(cachePath + File.separator + layerName, String.format("%d-%d-%d", z, x, y) + ".mvt");
        if (!file.exists()) {
            double[] bboxs = calBbox(x, y, z);
            String sql = "SELECT t.* FROM " + layerName + " t  WHERE ST_Intersects (shape,ST_MakeEnvelope(" + bboxs[1] + "," + bboxs[0] + "," + bboxs[3] + "," + bboxs[2] + ",4326))";

            List<RegionCounty> regionCountyList = sqlManager.execute(new SQLReady(sql), RegionCounty.class);
            GetMapOutputFormat format = new VectorTileMapOutputFormat(new MapBoxTileBuilderFactory());
            WMSMapContent mapContent = new WMSMapContent(256, 256, 256, 0, 32);

            try {
                CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
                ReferencedEnvelope envelope = new ReferencedEnvelope(bboxs[1], bboxs[3], bboxs[0], bboxs[2], crs);
                mapContent.setViewport(new MapViewport(envelope));


                ListFeatureCollection featureCollection = new ListFeatureCollection(FeatureCollections.newCollection());
                List<AttributeDescriptor> attributeDescriptors = new ArrayList<>();
                AtomicReference<SimpleFeatureTypeImpl> featureType = new AtomicReference<>();
                AtomicReference<GeometryDescriptor> geometryDescriptor = new AtomicReference<>();
                List<Field> fields = new ArrayList<>();
                ReflectionUtils.doWithFields(RegionCounty.class, field -> {
                    AttributeType type = null;
                    if (field.getName().equalsIgnoreCase("id")) {
                        type = new AttributeTypeImpl(new NameImpl(field.getName()), field.getType(), true, false, null, null, null);
                    } else if (field.getName().equalsIgnoreCase("shape")) {
                        type = new GeometryTypeImpl(new NameImpl("shape"), Polygon.class, crs, false, false, null, null, null);
                        geometryDescriptor.set(new GeometryDescriptorImpl((GeometryType) type, new NameImpl("shape"), 0, 0, false, null));
                    } else {
                        type = new AttributeTypeImpl(new NameImpl(field.getName()), field.getType(), false, false, null, null, null);
                    }
                    attributeDescriptors.add(new AttributeDescriptorImpl(type, new NameImpl(field.getName()), 0, 0, false, null));

                    fields.add(field);
                });
                featureType.set(new SimpleFeatureTypeImpl(new NameImpl("feature"), attributeDescriptors, geometryDescriptor.get(), false, null, null, null));


                regionCountyList.forEach(regionCounty -> {
                    SimpleFeature feature = new SimpleFeatureImpl(transBean2Map(regionCounty, fields), featureType.get(), new FeatureIdImpl(String.valueOf(regionCounty.getId())));
                    featureCollection.add(feature);
                });

                if (regionCountyList.size() > 0) {
                    List<Layer> layers = new ArrayList<>();
                    layers.add(new FeatureLayer(featureCollection, null, layerName));
                    byte[] res = format.produceMap(mapContent, layers, crs);

                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(res, 0, res.length);
                    fos.flush();
                    fos.close();
                    mapContent.dispose();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
            } catch (FactoryException e) {
                e.printStackTrace();
            }
        }
        return "forward:/vt/download/" + layerName + "/" + file.getName();
    }

    public double[] calBbox(int x, int y, int z) {
        return globalGeodetic.tileLatLonBounds(x, y, z);
    }

    @RequestMapping(
            value = "download/{layerName}/{fileName}",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
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
