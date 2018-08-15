define(['json!model/data/abc.json'], function (Abc) {
    var map, view, projection, baseLayer, regionCountyLayer, waterLineLayer, poiVillageLayer, tempLayer;
    var resolutions = [0.703125, 0.3515625, 0.17578125, 0.087890625, 0.0439453125, 0.02197265625, 0.010986328125, 0.0054931640625, 0.00274658203125, 0.001373291015625, 6.866455078125E-4, 3.4332275390625E-4, 1.71661376953125E-4, 8.58306884765625E-5, 4.291534423828125E-5, 2.1457672119140625E-5, 1.0728836059570312E-5, 5.364418029785156E-6, 2.682209014892578E-6, 1.341104507446289E-6, 6.705522537231445E-7, 3.3527612686157227E-7];
    var gridNames = ['EPSG:4326:0', 'EPSG:4326:1', 'EPSG:4326:2', 'EPSG:4326:3', 'EPSG:4326:4', 'EPSG:4326:5', 'EPSG:4326:6', 'EPSG:4326:7', 'EPSG:4326:8', 'EPSG:4326:9', 'EPSG:4326:10', 'EPSG:4326:11', 'EPSG:4326:12', 'EPSG:4326:13', 'EPSG:4326:14', 'EPSG:4326:15', 'EPSG:4326:16', 'EPSG:4326:17', 'EPSG:4326:18', 'EPSG:4326:19', 'EPSG:4326:20', 'EPSG:4326:21'];

    var selection = {};
    projection = new ol.proj.Projection({
        code: "EPSG:4326",
        units: "degrees",
        axisOrientation: 'neu',
        global: true
    });
    //建立地图视图
    view = new ol.View({
        center: [0, 0],
        zoom: 2,
        projection: projection,
        extent: [-180.0, -90.0, 180.0, 90.0]
    });

    baseLayer = new ol.layer.Tile({
        extent: [97.52865599987456, 21.142702999943538, 106.19671199955917, 29.25132500004878],
        source: new ol.source.TileArcGISRest({
            url: 'http://10.111.106.82:6080/arcgis/rest/services/yunnan_vector/MapServer',
            params: {
                time: (new Date("2011/01/24 00:00:00 UTC").getTime()) + "," + (new Date("2012/07/16 00:00:00 UTC").getTime())
            }
        })
    });

    // baseLayer = new ol.layer.Tile({
    //     source: new ol.source.OSM(),
    //     projection: projection
    // })

    waterLineLayer = new ol.layer.VectorTile({
        visible: true,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/waterLine/line2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=water_line',
            projection: projection,
            extent: ol.proj.EPSG4326.EXTENT,
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
    })

    regionCountyLayer = new ol.layer.VectorTile({
        visible: false,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/regionCounty/polygon2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=region_county',
            projection: projection,
            extent: ol.proj.EPSG4326.EXTENT,
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
    })

    poiVillageLayer = new ol.layer.VectorTile({
        visible: false,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/poiVillage/poi2/{z}/{x}/{-y}.mvt?srsname=' + projection.getCode() + '&layerName=poi_village',
            projection: projection,
            extent: ol.proj.EPSG4326.EXTENT,
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
    })

    tempLayer = new ol.layer.Vector({
        source: new ol.source.Vector({wrapX: true}),
        style: new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: "#ff0000",
                width: 4
            }),
            fill: new ol.style.Fill({
                color: "#ffff00"
            }),
            image: new ol.style.Circle({
                width: 5,
                color: "#ffff00"
            })
        })
    })

    map = new ol.Map({
        target: "map",
        layers: [baseLayer, regionCountyLayer, waterLineLayer, poiVillageLayer, tempLayer],
        view: view,
        projection: projection
    })

    map.getView().fit([97.528656, 21.142703, 106.196712, 29.251325], map.getSize());

    map.on('click', function (event) {
        var features = map.getFeaturesAtPixel(event.pixel);
        if (!features) {
            return;
        } else {
            if (features[0]) {
                tempLayer.getSource().clear(true);
                var fea;
                if (features[0].getType() == "Point") {
                    fea = new ol.Feature({
                        geometry: new ol.geom.Point(features[0].getFlatCoordinates())
                    })
                } else if (features[0].getType() == "LineString") {
                    var flatCoords = features[0].getFlatCoordinates();
                    var coords = [], coord = [];
                    for (var i = 0; i < flatCoords.length; i++) {
                        if (i % 2 == 0) {
                            coord.push(flatCoords[i]);
                        } else if (i % 2 == 1) {
                            coord.push(flatCoords[i]);
                            coords.push(coord);
                            coord = [];
                        }
                    }
                    fea = new ol.Feature({
                        geometry: new ol.geom.LineString(coords)
                    })
                    coords = [], flatCoords = [], coord = [];
                } else if (features[0].getType() == "Polygon") {
                    var flatCoords = features[0].getFlatCoordinates();
                    var coords = [], coord = [];
                    for (var i = 0; i < flatCoords.length; i++) {
                        if (i % 2 == 0) {
                            coord.push(flatCoords[i]);
                        } else if (i % 2 == 1) {
                            coord.push(flatCoords[i]);
                            coords.push(coord);
                            coord = [];
                        }
                    }
                    fea = new ol.Feature({
                        geometry: new ol.geom.Polygon([coords])
                    })
                    coords = [], flatCoords = [], coord = [];
                }
                tempLayer.getSource().addFeature(fea);
                tempLayer.getSource().dispatchEvent("addfeature");
            }
        }
    });
})