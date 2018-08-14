package org.fengsoft.jts2geojson.tile;

/**
 * @Author JerFer
 * @Date 2018/8/14---16:54
 */
/**
 * Created by JerFer
 * Date: 2017/12/13.
 */
public class GlobalMercator {
    private int tileSize;
    private double initialResolution;
    private double originShift;

    public GlobalMercator(int tileSize) {
        this.tileSize = tileSize;
        this.initialResolution = 2 * Math.PI * 6378137 / this.tileSize;
        this.originShift = 2 * Math.PI * 6378137 / 2.0;
    }

    public double[] latLonToMeters(double lat, double lon) {
        double mx = lon * this.originShift / 180.0;
        double my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        my = my * this.originShift / 180.0;
        return new double[]{mx, my};
    }

    public double[] metersToLatLon(double mx, double my) {
        double lon = (mx / this.originShift) * 180.0;
        double lat = (my / this.originShift) * 180.0;

        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
        return new double[]{lat, lon};
    }

    public double[] pixelsToMeters(int px, int py, int zoom) {
        double res = this.resolution(zoom);
        double mx = px * res - this.originShift;
        double my = py * res - this.originShift;
        return new double[]{mx, my};
    }

    public double[] metersToPixels(double mx, double my, int zoom) {
        double res = this.resolution(zoom);
        double px = (mx + this.originShift) / res;
        double py = (my + this.originShift) / res;
        return new double[]{px, py};
    }

    public int[] pixelsToTile(double px, double py) {
        int tx = (int) (Math.ceil(px / (float) (this.tileSize)) - 1);
        int ty = (int) (Math.ceil(py / (float) (this.tileSize)) - 1);
        return new int[]{tx, ty};
    }

    public double[] pixelsToRaster(double px, double py, int zoom) {
        double mapSize = this.tileSize << zoom;
        return new double[]{px, mapSize - py};
    }

    public int[] metersToTile(double mx, double my, int zoom) {
        double[] coordinate = this.metersToPixels(mx, my, zoom);
        return this.pixelsToTile(coordinate[0], coordinate[1]);
    }

    public double[] tileBounds(int tx, int ty, int zoom) {
        double[] minxy = pixelsToMeters(tx * this.tileSize, ty * this.tileSize, zoom);
        double[] maxxy = pixelsToMeters((tx + 1) * this.tileSize, (ty + 1) * this.tileSize, zoom);

        return new double[]{minxy[0], minxy[1], maxxy[0], maxxy[1]};
    }

    public double[] tileLatLonBounds(int tx, int ty, int zoom) {
        double[] bounds = this.tileBounds(tx, ty, zoom);
        double[] minLatLon =this. metersToLatLon(bounds[0], bounds[1]);
        double[] maxLatlon = this.metersToLatLon(bounds[2], bounds[3]);
        return new double[]{minLatLon[0], minLatLon[1], maxLatlon[0], maxLatlon[1]};
    }

    public double resolution(int zoom) {
        return this.initialResolution / (Math.pow(2, zoom));
    }

    public int zoomForPixelSize(double pixelSize) {
        for (int i = 0; i < ContentValue.MAXZOOMLEVEL; i++) {
            if (pixelSize > this.resolution(i)) {
                if (i != 0) {
                    return i - 1;
                } else return 0;
            }
        }
        return 0;
    }

    public int[] googleTile(int tx, int ty, int zoom) {
        return new int[]{tx, ((int) (Math.pow(2, zoom)) - 1) - ty};
    }

    public String quadTree(int tx, int ty, int zoom) {
        String quadKey = "";
        ty = (int) (Math.pow(2, zoom) - 1 - ty);
        for (int i = zoom; i > 0; i--) {
            int digit = 0;
            int mask = 1 << (i - 1);
            if ((tx & mask) != 0) {
                digit += 1;
            }
            if ((ty & mask) != 0) {
                digit += 2;
                quadKey += digit;
            }
        }
        return quadKey;
    }
}