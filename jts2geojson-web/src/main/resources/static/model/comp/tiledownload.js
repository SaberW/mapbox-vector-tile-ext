define(['map'], function (Map) {
    var map = Map.map, draw, projection = Map.projection, tempLayer = Map.tempLayer;

    var tileLayer = new ol.layer.Tile({
        source: new ol.source.XYZ({
            projection: ol.proj.get("EPSG:3857"),
            maxZoom: 23,
            minZoom: 0,
            url: contextPath + "/tilecache/tile/{x}/{-y}/{z}"
        }),
        projection: projection
    })

    map.addLayer(tileLayer);

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
})