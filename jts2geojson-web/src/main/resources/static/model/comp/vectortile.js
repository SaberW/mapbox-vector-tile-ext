define(function () {
    var map, projection, select, tempLayer, waterLineLayer, regionCountyLayer, poiVillageLayer;
    var Map = require("model/map")
    map = Map.map;
    projection = Map.projection;
    tempLayer = Map.tempLayer;

    waterLineLayer = new ol.layer.VectorTile({
        visible: false,
        renderMode: "image",
        preload: 12,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/waterLine/line2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=water_line',
            projection: projection,
            extent: ol.proj.get("EPSG:4326").getExtent(),
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
    })

    regionCountyLayer = new ol.layer.VectorTile({
        visible: true,
        renderMode: "image",
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/regionCounty/polygon2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=region_county',
            projection: projection,
            extent: ol.proj.get("EPSG:4326").getExtent(),
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
    })

    poiVillageLayer = new ol.layer.VectorTile({
        visible: false,
        renderMode: "image",
        preload: 12,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/poiVillage/poi2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=poi_village',
            projection: projection,
            extent: ol.proj.get("EPSG:4326").getExtent(),
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
    })

    select = new ol.interaction.Select();
    select.on("select", function (e) {
        tempLayer.getSource().clear(true);
        if (e.target.getFeatures().getLength() > 0) {
            var fea, targetFea = e.target.getFeatures().getArray()[0], newGeom;
            var flatCoords = targetFea.getFlatCoordinates();
            var coords = [], coord = [];
            if (targetFea.getType() == "Point") {
                newGeom = new ol.geom.Point(targetFea.getFlatCoordinates())
            } else if (targetFea.getType() == "LineString") {
                for (var i = 0; i < flatCoords.length; i++) {
                    if (i % 2 == 0) {
                        coord.push(flatCoords[i]);
                    } else if (i % 2 == 1) {
                        coord.push(flatCoords[i]);
                        coords.push(coord);
                        coord = [];
                    }
                }
                newGeom = new ol.geom.LineString(coords);
            } else if (targetFea.getType() == "Polygon") {
                for (var i = 0; i < flatCoords.length; i++) {
                    if (i % 2 == 0) {
                        coord.push(flatCoords[i]);
                    } else if (i % 2 == 1) {
                        coord.push(flatCoords[i]);
                        coords.push(coord);
                        coord = [];
                    }
                }
                newGeom = new ol.geom.Polygon([coords])
            }
            coords = [], flatCoords = [], coord = [];
            fea = new ol.Feature({
                geometry: newGeom,
                name: targetFea.get("name")
            })
            tempLayer.getSource().addFeature(fea);
            tempLayer.getSource().dispatchEvent("addfeature");
        }
    })
    map.addInteraction(select);

    //事件：抓
    map.on('pointerdrag', function (evt) {
        select.setActive(false);
    });
    //事件：地图移动结束
    map.on('moveend', function (evt) {
        select.setActive(true);
    });
})