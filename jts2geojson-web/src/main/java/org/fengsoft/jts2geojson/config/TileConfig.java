package org.fengsoft.jts2geojson.config;

import org.fengsoft.geojson.common.GlobalMercator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TileConfig {
    @Bean
    public GlobalMercator mercator() {
        return new GlobalMercator(256);
    }
}
