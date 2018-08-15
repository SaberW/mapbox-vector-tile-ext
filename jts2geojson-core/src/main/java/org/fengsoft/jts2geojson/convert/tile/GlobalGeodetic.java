package org.fengsoft.jts2geojson.convert.tile;

/**
 * @Author JerFer
 * @Date 2018/8/14---16:52
 */
public class GlobalGeodetic {
    private int tileSize;
    private double resFact;

    public GlobalGeodetic(String tmscompatible, int tileSize) {
        this.tileSize = tileSize;
        // Defaults the resolution factor to 0.703125 (2 tiles @ level 0)
        if (tmscompatible != null && tmscompatible.length() > 0) {
            this.resFact = 180.0 / this.tileSize;
        } else {
            //Defaults the resolution factor to 1.40625 (1 tile @ level 0)
            this.resFact = 360.0 / this.tileSize;
        }
    }

    public double[] lonlatToPixels(double lon, double lat, int zoom) {
        double res = this.resFact / Math.pow(2, zoom);
        return new double[]{((180.0 + lon) / res), ((90.0 + lat) / res)};
    }

    public int[] pixelsToTile(double px, double py) {
        int tx = (int) (Math.ceil(px / (double) (this.tileSize)) - 1);
        int ty = (int) (Math.ceil(py / (double) (this.tileSize)) - 1);
        return new int[]{tx, ty};
    }

    public int[] lonlatToTile(double lon, double lat, int zoom) {
        double[] pxpy = this.lonlatToPixels(lon, lat, zoom);
        return this.pixelsToTile(pxpy[0], pxpy[1]);
    }

    public double resolution(int zoom) {
        return this.resFact / Math.pow(2, zoom);
    }

    public int zoomForPixelSize(double pixelSize) {
        for (int i = 0; i < ContentValue.MAXZOOMLEVEL; i++) {
            if (pixelSize > resolution(i)) {
                if (i != 0) return i - 1;
                else return 0;
            }
        }
        return 0;
    }

    public double[] tileBounds(int tx, int ty, int zoom) {
        double res = this.resFact / Math.pow(2, zoom);
        return new double[]{tx * this.tileSize * res - 180,
                ty * this.tileSize * res - 90,
                (tx + 1) * this.tileSize * res - 180,
                (ty + 1) * this.tileSize * res - 90};
    }

    public double[] tileLatLonBounds(int tx, int ty, int zoom) {
        double[] b = this.tileBounds(tx, ty, zoom);
        return new double[]{b[1], b[0], b[3], b[2]};
    }
}
