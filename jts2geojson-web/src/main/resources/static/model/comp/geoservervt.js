define(['map'], function (Map) {
    var map = Map.map, projection = Map.projection, select, tempLayer = Map.tempLayer, regionCounty, waterLine,
        baseLayer,
        poiVillage, vectorTileGrid;

    // regionCounty = new ol.layer.VectorTile({
    //     renderMode: "image",
    //     preload: 12,
    //     source: new ol.source.VectorTile({
    //         format: new ol.format.MVT(),
    //         url: contextPath + '/geoserver/vt/{z}/{x}/{-y}.mvt?layerName=region_county',
    //         projection: projection,
    //         extent: ol.proj.get("EPSG:4326").getExtent(),
    //         tileSize: 256,
    //         maxZoom: 21,
    //         minZoom: 0,
    //         wrapX: true
    //     }),
    //     style: function (fea, proj, abc) {
    //         return new ol.style.Style({
    //             fill: new ol.style.Fill({
    //                 color: 'rgba(255,255,0,0.4)'
    //             }),
    //             stroke: new ol.style.Stroke({
    //                 color: '#ff0000',
    //                 width: 2
    //             }),
    //             text: new ol.style.Text({
    //                 text: fea.getProperties().name
    //             })
    //         })
    //     }
    // })
    //
    // map.addLayer(regionCounty);
    //
    // waterLine = new ol.layer.VectorTile({
    //     renderMode: "image",
    //     preload: 12,
    //     source: new ol.source.VectorTile({
    //         format: new ol.format.MVT(),
    //         url: contextPath + '/geoserver/vt/{z}/{x}/{-y}.mvt?layerName=water_line',
    //         projection: projection,
    //         extent: ol.proj.get("EPSG:4326").getExtent(),
    //         tileSize: 256,
    //         maxZoom: 21,
    //         minZoom: 0,
    //         wrapX: true
    //     })
    // })
    //
    // map.addLayer(waterLine);
    //
    // poiVillage = new ol.layer.VectorTile({
    //     renderMode: "image",
    //     preload: 12,
    //     source: new ol.source.VectorTile({
    //         format: new ol.format.MVT(),
    //         url: contextPath + '/geoserver/vt/{z}/{x}/{-y}.mvt?layerName=poi_village',
    //         projection: projection,
    //         extent: ol.proj.get("EPSG:4326").getExtent(),
    //         tileSize: 256,
    //         maxZoom: 21,
    //         minZoom: 0,
    //         wrapX: true
    //     })
    // })
    //
    // map.addLayer(poiVillage);

    baseLayer = new ol.layer.VectorTile({
        renderMode: "image",
        preload: 12,
        source: new ol.source.VectorTile({
            format: new ol.format.MVT(),
            url: contextPath + '/geoserver/vt/{z}/{x}/{-y}.mvt?layerName=baseLayer',
            projection: projection,
            extent: ol.proj.get("EPSG:4326").getExtent(),
            tileSize: 256,
            maxZoom: 21,
            minZoom: 0,
            wrapX: true
        }),
        style: function (fea, res) {
            if (fea.getType() === "Polygon" || fea.getType() === "MultiPolygon")
                return new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#ff0000',
                        width: 2
                    }),
                    text: new ol.style.Text({
                        text: fea.getProperties().name
                    })
                })
            else if (fea.getType() === "Point" || fea.getType() === "MultiPoint") {
                return new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: '#0000ff',
                    }),
                    stroke: new ol.style.Stroke({
                        width: 2
                    })
                })
            } else if (fea.getType() === "LineString" || fea.getType() === "MultiLineString") {
                return new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#ff0000',
                    }),
                })
            }
        }
    })

    map.addLayer(baseLayer);

    // vectorTileGrid = new ol.layer.Tile({
    //     source: new ol.source.TileDebug({
    //         projection: 'EPSG:3857',
    //         tileGrid: regionCounty.getSource().getTileGrid()
    //     }),
    //     style: new ol.style.Style({
    //         stroke: new ol.style.Stroke({
    //             color: "#ff0000",
    //             width: 1
    //         })
    //     })
    // })
    //
    // map.addLayer(vectorTileGrid)

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
    map.on("click", function (event) {
        console.log(event.coordinate)
    })
    map.addInteraction(select);
})