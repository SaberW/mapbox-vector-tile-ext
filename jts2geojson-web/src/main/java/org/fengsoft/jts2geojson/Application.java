package org.fengsoft.jts2geojson;

import org.fengsoft.jts2geojson.common.TileType;
import org.fengsoft.jts2geojson.services.GenerateTileService;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private GenerateTileService generateTileService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        generateTileService.run("tianditu", new Envelope(97.4853057861328, 106.240058898926, 21.1021595001221, 29.2918682098389), "EPSG:4326", TileType.TDTVEC);
    }
}
