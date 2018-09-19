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
//        generateTileService.run("tiles", new Envelope(97.4853057861328, 106.240058898926, 21.1021595001221, 29.2918682098389), "EPSG:4326", TileType.TDT, false);
//
//        ((Runnable) () -> new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                long total = generateTileService.getTotalCount(TileType.TDT);
//                long vcrSuccess = generateTileService.getCountVCRsuccess().longValue();
//                long vcrError = generateTileService.getCountVCRerror().longValue();
//                long vecSuccess = generateTileService.getCountVECsuccess().longValue();
//                long vecError = generateTileService.getCountVECerror().longValue();
//                log.info(String.format(" VCR（%d-%d-%d）--------VEC（%d-%d-%d）--------done %d",
//                        total / 2,
//                        vcrSuccess,
//                        vcrError,
//                        total / 2,
//                        vecSuccess,
//                        vecError,
//                        (vcrSuccess + vcrError + vecSuccess + vecError)
//                ));
//            }
//        }, new Date(), 5000)).run();
    }
}
