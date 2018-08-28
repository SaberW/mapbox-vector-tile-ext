package org.fengsoft.jts2geojson.web;

import org.fengsoft.jts2geojson.common.TileType;
import org.fengsoft.jts2geojson.services.GenerateTileService;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "generate")
public class GenerateTileController {
    @Autowired
    private GenerateTileService generateTileService;

    /**
     * 102.7687099999999987, 102.7813691098872226, 25.0036402053958895, 25.0139100000000028
     *
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     */
    @RequestMapping(value = "tile")
    public void tile(String tileName, Double xmin, Double xmax, Double ymin, Double ymax) {
        Envelope envelope = new Envelope(xmin, xmax, ymin, ymax);
        generateTileService.run(tileName, envelope, "EPSG:4326", TileType.GOOGLEIMAGE);
    }
}
