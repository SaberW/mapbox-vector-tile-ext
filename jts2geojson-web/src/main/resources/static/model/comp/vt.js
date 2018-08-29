define(['map'], function (Map) {
    var map = Map.map, projection = Map.projection,tempLayer = Map.tempLayer;

    var gridsetName = 'EPSG:4326';
    var gridNames = ['EPSG:4326:0', 'EPSG:4326:1', 'EPSG:4326:2', 'EPSG:4326:3', 'EPSG:4326:4', 'EPSG:4326:5', 'EPSG:4326:6', 'EPSG:4326:7', 'EPSG:4326:8', 'EPSG:4326:9', 'EPSG:4326:10', 'EPSG:4326:11', 'EPSG:4326:12', 'EPSG:4326:13', 'EPSG:4326:14', 'EPSG:4326:15', 'EPSG:4326:16', 'EPSG:4326:17', 'EPSG:4326:18', 'EPSG:4326:19', 'EPSG:4326:20', 'EPSG:4326:21'];
    var baseUrl = 'http://localhost:7080/geoserver/gwc/service/wmts';
    var style = '';
    var format = 'application/x-protobuf;type=mapbox-vector';
    var infoFormat = 'text/html';
    var layerName = 'gis:region_county';
    var resolutions = [0.703125, 0.3515625, 0.17578125, 0.087890625, 0.0439453125, 0.02197265625, 0.010986328125, 0.0054931640625, 0.00274658203125, 0.001373291015625, 6.866455078125E-4, 3.4332275390625E-4, 1.71661376953125E-4, 8.58306884765625E-5, 4.291534423828125E-5, 2.1457672119140625E-5, 1.0728836059570312E-5, 5.364418029785156E-6, 2.682209014892578E-6, 1.341104507446289E-6, 6.705522537231445E-7, 3.3527612686157227E-7];
    var params = {
        'REQUEST': 'GetTile',
        'SERVICE': 'WMTS',
        'VERSION': '1.0.0',
        'LAYER': layerName,
        'STYLE': style,
        'TILEMATRIX': gridsetName + ':{z}',
        'TILEMATRIXSET': gridsetName,
        'FORMAT': format,
        'TILECOL': '{x}',
        'TILEROW': '{y}'
    };

    function constructSource() {
        var url = baseUrl + '?'
        for (var param in params) {
            url = url + param + '=' + params[param] + '&';
        }
        url = url.slice(0, -1);

        var source = new ol.source.VectorTile({
            url: url,
            format: new ol.format.MVT({}),
            projection: projection,
            tileGrid: new ol.tilegrid.WMTS({
                tileSize: [256, 256],
                origin: [-180.0, 90.0],
                resolutions: resolutions,
                matrixIds: gridNames
            }),
            wrapX: true
        });
        return source;
    }

    var layer = new ol.layer.VectorTile({
        source: constructSource()
    });
    map.addLayer(layer)

    map.getView().fit([97.4853057861328, 21.1021595001221, 106.240058898926, 29.2918682098389], map.getSize());

    window.setParam = function (name, value) {
        if (name == "STYLES") {
            name = "STYLE"
        }
        params[name] = value;
        layer.setSource(constructSource());
        map.updateSize();
    }

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