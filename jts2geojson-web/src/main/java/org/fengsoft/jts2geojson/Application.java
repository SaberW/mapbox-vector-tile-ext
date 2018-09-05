package org.fengsoft.jts2geojson;

import lombok.extern.slf4j.Slf4j;
import org.fengsoft.jts2geojson.common.TileType;
import org.fengsoft.jts2geojson.services.GenerateTileService;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {
    @Autowired
    private GenerateTileService generateTileService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        generateTileService.run("tiles", new Envelope(97.4853057861328, 106.240058898926, 21.1021595001221, 29.2918682098389), "EPSG:4326", TileType.TDT, false);

        ((Runnable) () -> new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log.info(String.format(" VCR(%d-%d-%d)--------VEC(%d-%d-%d)--------done %d",
                        generateTileService.getTotalCount(TileType.TDT) / 2, generateTileService.getCountVCRsuccess().longValue(), generateTileService.getCountVCRerror().longValue(),
                        generateTileService.getTotalCount(TileType.TDT) / 2, generateTileService.getCountVECsuccess().longValue(), generateTileService.getCountVECerror().longValue()),
                        generateTileService.getTotalCount(TileType.TDT) - (
                                generateTileService.getCountVCRsuccess().longValue() +
                                        generateTileService.getCountVCRerror().longValue() +
                                        generateTileService.getCountVECsuccess().longValue() +
                                        generateTileService.getCountVECerror().longValue())
                );
            }
        }, new Date(), 5000)).run();
    }
}
