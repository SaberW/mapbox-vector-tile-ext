package org.fengsoft.jts2geojson.common;

/**
 * @Author JerFer
 * @Date 2018/8/28---15:00
 */
public enum TileType {
    GOOGLE("google", "http://www.google.cn/maps/vt/pb=!1m4!1m3!1i%d!2i%d!3i%d!2m3!1e0!2sm!3i380072576!3m8!2szh-CN!3scn!5e1105!12m4!1e68!2m2!1sset!2sRoadmap!4e0!5m1!1e0"),
    GOOGLEIMAGE("googleimage", "http://www.google.cn/maps/vt?lyrs=s@804&gl=cn&x=%d&y=%d&z=%d"),
    BING("bing", "https://dynamic.t0.tiles.ditu.live.com/comp/ch/%s?mkt=zh-CN&ur=cn&it=G,TW,BX,L&cstl=w4c"),
    OSM("osm", "https://c.tile.openstreetmap.org/%d/%d/%d.png"),
    TDTCVR("tdtcvr", "http://t3.tianditu.gov.cn/DataServer?T=cva_w&x=%d&y=%d&l=%d"),
    TDTVEC("tdtvec", "http://t3.tianditu.gov.cn/DataServer?T=vec_w&x=%d&y=%d&l=%d");

    private String url;
    private String type;

    TileType(String type, String url) {
        this.url = url;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
