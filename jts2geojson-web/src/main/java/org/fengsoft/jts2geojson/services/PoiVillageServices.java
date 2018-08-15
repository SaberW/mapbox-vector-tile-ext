package org.fengsoft.jts2geojson.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.*;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;
import lombok.extern.slf4j.Slf4j;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLReady;
import org.fengsoft.jts2geojson.convert.services.GeoJsonServicesImpl;
import org.fengsoft.jts2geojson.entity.PoiVillage;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author JerFer
 * @Date 2018/8/1---9:43
 */
@Service
@Transactional
@Slf4j
public class PoiVillageServices extends GeoJsonServicesImpl<PoiVillage, Integer> {
    @Autowired
    private SQLManager sqlManager;

    public String allFeatures(String srsname, String bbox) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(toFeatures(sqlManager.all(PoiVillage.class)));
    }

    public void listFeature(String srsname, String layerName, Integer x, Integer y, Integer z) {
        //计算范围
        double[] bboxs = init(srsname, x, y, z);
        String sql = getSql(bboxs, layerName, srsname);
        SQLReady sqlReady = new SQLReady(sql);
        List<PoiVillage> res = sqlManager.execute(sqlReady, PoiVillage.class);

        MvtLayerParams layerParams = new MvtLayerParams(); // Default extent
        /////////////////////////////////
        VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();
        VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder(layerName, layerParams);

        IGeometryFilter acceptAllGeomFilter = geom -> true;
        List<Geometry> geometries = res.stream().map(poiVillage -> {
            try {
                return wkbReader.read(WKBReader.hexToBytes(((PGobject) (poiVillage.getShape())).getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(a -> a != null).collect(Collectors.toList());

        Envelope tileEnvelope = new Envelope(bboxs[1], bboxs[3], bboxs[0], bboxs[2]);

        TileGeomResult tileGeom = JtsAdapter.createTileGeom(geometries, tileEnvelope, geometryFactory, layerParams, acceptAllGeomFilter);

        MvtLayerProps layerProps = new MvtLayerProps();

        IUserDataConverter userDataConverter = new UserDataKeyValueMapConverter();
        List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, userDataConverter);
        layerBuilder.addAllFeatures(features);
        tileBuilder.addLayers(layerBuilder.build());
        MvtLayerBuild.writeProps(layerBuilder, layerProps);
        VectorTile.Tile mvt = tileBuilder.build();

        try {
            Files.write(Paths.get(cachePath, layerName, String.format("%d-%d-%d", z, x, y) + ".mvt"), mvt.toByteArray());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
