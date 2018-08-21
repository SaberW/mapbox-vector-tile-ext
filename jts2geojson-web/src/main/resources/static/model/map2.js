define(['json!model/data/abc.json'], function (Abc) {
    var map, view, projection, baseLayer, gridLayer, regionCountyLayer, waterLineLayer, poiVillageLayer, tempLayer;
    var select;
    projection = new ol.proj.Projection({
        code: "EPSG:4326",
        units: "degrees",
        axisOrientation: 'neu',
        global: true
    });
    //建立地图视图
    view = new ol.View({
        center: [0, 0],
        zoom: 12,
        projection: projection,
        extent: [-180.0, -90.0, 180.0, 90.0]
    });

    // baseLayer = new ol.layer.Tile({
    //     extent: [97.52865599987456, 21.142702999943538, 106.19671199955917, 29.25132500004878],
    //     source: new ol.source.TileArcGISRest({
    //         url: 'http://10.111.106.82:6080/arcgis/rest/services/yunnan_vector/MapServer',
    //         params: {
    //             time: (new Date("2011/01/24 00:00:00 UTC").getTime()) + "," + (new Date("2012/07/16 00:00:00 UTC").getTime())
    //         }
    //     })
    // });

    baseLayer = new ol.layer.Tile({
        source: new ol.source.OSM({
            projection: projection
        }),
        projection: projection
    })

    gridLayer = new ol.layer.Tile({
        //瓦片网格数据源
        source: new ol.source.TileDebug({
            //投影
            projection: 'EPSG:4326',
            //获取瓦片网格信息
            tileGrid: baseLayer.getSource().getTileGrid()
        })
    })

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
            wrapX: true
        }),
    })

    regionCountyLayer = new ol.layer.VectorTile({
        visible: true,
        renderMode: "image",
        preload: 12,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/regionCounty/polygon2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=region_county',
            projection: projection,
            extent: ol.proj.get("EPSG:4326").getExtent(),
            tileSize: 256,
            maxZoom: 21,
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
            wrapX: true
        }),
    })

    tempLayer = new ol.layer.Vector({
        source: new ol.source.Vector({wrapX: true}),
        style: function (feature, res) {
            return new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: "#ff0000",
                    width: 4
                }),
                fill: new ol.style.Fill({
                    color: "#ffff00"
                }),
                image: new ol.style.Circle({
                    radius: 6,   //填充图案样式
                    fill: new ol.style.Fill({color: '#ffcc33'}),
                    stroke: new ol.style.Stroke({
                        color: "#ff0000",
                        width: 2
                    }),
                }),
                text: new ol.style.Text({
                    text: feature.get("name"),
                    font: 'normal normal bold 12px arial,sans-serif',
                    offsetY: -30,
                    fill: new ol.style.Fill({color: '#000000'})
                })
            })
        }
    })

    map = new ol.Map({
        target: "map",
        layers: [baseLayer,
            regionCountyLayer, waterLineLayer, poiVillageLayer,
            // gridLayer,
            tempLayer],
        view: view,
        projection: projection
    })

    map.getView().fit([97.528656 + 1, 21.142703 + 1, 106.196712 + 1, 29.251325 + 1], map.getSize());

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

    return map;
})