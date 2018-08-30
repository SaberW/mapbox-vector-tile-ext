define(['map'], function (Map) {
    var map = Map.map, draw, projection = Map.projection, tempLayer = Map.tempLayer;

    function addDownloadLayer(tileName) {
        var tileLayer = new ol.layer.Tile({
            source: new ol.source.XYZ({
                projection: ol.proj.get("EPSG:3857"),
                maxZoom: 23,
                minZoom: 0,
                url: contextPath + "/generate/tile/" + tileName + "/{x}/{-y}/{z}"
            }),
            projection: projection
        })

        map.addLayer(tileLayer);
    }

    function addInteraction() {
        if (draw) map.removeInteraction(draw)
        draw = new ol.interaction.Draw({
            source: tempLayer.getSource(),
            type: "Circle",
            geometryFunction: ol.interaction.Draw.createBox()
        });
        draw.on("drawend", function (re) {
            var ext = re.feature.getGeometry().getExtent()
            ext = [97.4853057861328, 21.1021595001221, 106.240058898926, 29.2918682098389];
            tempLayer.getSource().clear();
            var tileName = $("#tool-draw-tile-name").val();
            if (tileName) {
                $.ajax({
                    url: contextPath + "/generate/download",
                    data: {
                        tileName: tileName,
                        xmin: ext[0],
                        xmax: ext[2],
                        ymin: ext[1],
                        ymax: ext[3]
                    },
                    type: "GET"
                }).done(function (re) {
                    console.log(re)
                });
                addDownloadLayer(tileName)
            }
        })
        map.addInteraction(draw);
    }

    $(function () {
        $("#tool-draw").on("click", function () {
            addInteraction();
        })

        $("#tool-add").on("click", function () {
            var tileName = $("#tool-draw-tile-name").val()
            if (tileName)
                addDownloadLayer(tileName)
        });
    })
})