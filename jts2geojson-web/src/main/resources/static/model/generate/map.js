define(['json!model/data/abc.json'], function () {
    var map, view, projection, baseLayer, tileLayer, tempLayer, draw;
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

    baseLayer = new ol.layer.Tile({
        source: new ol.source.OSM(),
        projection: projection
    })

    // baseLayer=new ol.layer.Tile({
    //     source:new ol.source.XYZ({
    //         //    http://www.google.cn/maps/vt/pb=!1m4!1m3!1i15!2i25734!3i14033!2m3!1e0!2sm!3i380072576!3m8!2szh-CN!3scn!5e1105!12m4!1e68!2m2!1sset!2sRoadmap!4e0!5m1!1e0
    //         url: 'http://www.google.cn/maps/vt/pb=!1m4!1m3!1i{z}!2i{x}!3i{y}!2m3!1e0!2sm!3i380072576!3m8!2szh-CN!3scn!5e1105!12m4!1e68!2m2!1sset!2sRoadmap!4e0!5m1!1e0'
    //     }),
    //     projection: projection
    // })

    var tiled = new ol.layer.Tile({
        source: new ol.source.TileWMS({
            url: 'http://localhost:7080/geoserver/gis/wms',
            params: {
                'FORMAT': "image/png",
                'VERSION': '1.1.1',
                tiled: true,
                STYLES: '',
                LAYERS: 'gis:load_highway',
                tilesOrigin: 97.8315353393555 + "," + 21.1901741027832
            }
        })
    });
    var tiled2 = new ol.layer.Tile({
        source: new ol.source.TileWMS({
            url: 'http://localhost:7080/geoserver/gis/wms',
            params: {
                'FORMAT': "image/png",
                'VERSION': '1.1.1',
                tiled: true,
                STYLES: '',
                LAYERS: 'gis:water_line',
                tilesOrigin: 97.8315353393555 + "," + 21.1901741027832
            }
        })
    });


    tileLayer = new ol.layer.Tile({
        source: new ol.source.XYZ({
            projection: ol.proj.get("EPSG:3857"),
            maxZoom: 23,
            minZoom: 0,
            url: contextPath + "/tilecache/tile/{x}/{-y}/{z}"
        }),
        projection: projection
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
        layers: [new ol.layer.Tile({
            source: new ol.source.XYZ({
                title: "天地图路网",
                url: "http://t2.tianditu.com/DataServer?T=vec_w&x={x}&y={y}&l={z}"
            })
        }),
            new ol.layer.Tile({
                source: new ol.source.XYZ({
                    title: "天地图文字标注",
                    url: "http://t2.tianditu.com/DataServer?T=cva_w&x={x}&y={y}&l={z}"
                })
            }), tiled,tiled2, tempLayer],
        view: view,
        projection: projection
    })

    map.getView().fit([97.528656 + 1, 21.142703 + 1, 106.196712 + 1, 29.251325 + 1], map.getSize());

    map.getView().setCenter([102.788704, 24.993415]);
    map.getView().setZoom(16);

    function addInteraction() {
        if (draw) map.removeInteraction(draw)
        draw = new ol.interaction.Draw({
            source: tempLayer.getSource(),
            type: "Circle",
            geometryFunction: ol.interaction.Draw.createBox()
        });
        draw.on("drawend", function (re) {
            var ext = re.feature.getGeometry().getExtent()
            tempLayer.getSource().clear();
            $.ajax({
                url: contextPath + "/generate/tile",
                data: {
                    xmin: ext[0],
                    xmax: ext[2],
                    ymin: ext[1],
                    ymax: ext[3]
                },
                type: "GET"
            }).done(function (re) {
                console.log(re)
            })
        })
        map.addInteraction(draw);
    }

    $(function () {
        $("#tool-draw").on("click", function () {
            addInteraction();
        })
    })
});
