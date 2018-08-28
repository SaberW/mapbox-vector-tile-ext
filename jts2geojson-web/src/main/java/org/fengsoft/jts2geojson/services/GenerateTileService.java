package org.fengsoft.jts2geojson.services;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
import okhttp3.*;
import org.fengsoft.jts2geojson.common.TileType;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

@Service
public class GenerateTileService {
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private GlobalMercator mercator;
    @Value("${cache.image-tile-path}")
    private String imageTilePath;

    private LinkedList<int[]> tminmax;
    private double ominx;
    private double omaxx;
    private double ominy;
    private double omaxy;
    private int tminz = 1;
    private int tmaxz = 20;


    public void run(String tileName, Envelope envelope, String epsg, TileType tileType) {
        File file = new File(imageTilePath);
        if (!file.exists()) file.mkdirs();

        File subDir = new File(imageTilePath, tileName);
        if (!subDir.exists()) subDir.mkdir();

        mercator = new GlobalMercator(256);

        double[] min = mercator.latLonToMeters(envelope.getMinY(), envelope.getMinX());
        double[] max = mercator.latLonToMeters(envelope.getMaxY(), envelope.getMaxX());

        this.ominx = min[0];
        this.omaxx = max[0];
        this.omaxy = max[1];
        this.ominy = min[1];

        tminmax = new LinkedList<>();
        for (int tz = 0; tz < 32; tz++) {
            int[] tminxy = this.mercator.metersToTile(this.ominx, this.ominy, tz);
            int[] tmaxxy = this.mercator.metersToTile(this.omaxx, this.omaxy, tz);

            tminxy = new int[]{Math.max(0, tminxy[0]), Math.max(0, tminxy[1])};
            tmaxxy = new int[]{(int) Math.min(Math.pow(2, tz) - 1, tmaxxy[0]), (int) Math.min(Math.pow(2, tz) - 1, tmaxxy[1])};

            this.tminmax.add(tz, new int[]{tminxy[0], tminxy[1], tmaxxy[0], tmaxxy[1]});
        }

        int tminx = tminmax.get(tmaxz)[0];
        int tminy = tminmax.get(tmaxz)[1];
        int tmaxx = tminmax.get(tmaxz)[2];
        int tmaxy = tminmax.get(tmaxz)[3];

        int tz = tmaxz;

        for (int ty = tmaxy; ty > tminy - 1; ty--) {
            for (int tx = tminx; tx < tmaxx + 1; tx++) {
                generateTile(subDir.getAbsolutePath(), tx, ty, tz, tileType);
            }
        }

        for (tz = tmaxz - 1; tz > tminz - 1; tz--) {
            int[] tminxytmaxxy = tminmax.get(tz);
            for (int ty = tminxytmaxxy[3]; ty > tminxytmaxxy[1] - 1; ty--) {
                for (int tx = tminxytmaxxy[0]; tx < tminxytmaxxy[2] + 1; tx++) {
                    generateTile(subDir.getAbsolutePath(), tx, ty, tz, tileType);
                }
            }
        }
    }

    private void generateTile(String cacheDir, int tx, int ty, int tz, TileType tileType) {
        String url = "";
        int[] gootleXY = mercator.googleTile(tx, ty, tz);
        if (tileType.getType().equals(TileType.BING.getType())) {
            String code = mercator.tileXYToQuadKey(gootleXY[0], gootleXY[1], tz);
            url = String.format(tileType.getUrl(), code);
        } else if (tileType.getType().equals(TileType.GOOGLE.getType())) {
            url = String.format(tileType.getUrl(), tz, gootleXY[0], gootleXY[1]);
        } else if (tileType.getType().equals(TileType.OSM.getType())) {
            url = String.format(tileType.getUrl(), tz, tx, ty);
        } else if (tileType.getType().equals(TileType.TDTCVR.getType())) {
            url = String.format(tileType.getUrl(), tx, ty, tz);
        } else if (tileType.getType().equals(TileType.TDTVEC.getType())) {
            url = String.format(tileType.getUrl(), tx, ty, tz);
        }
        if (!StringUtils.isEmpty(url))
            savePng(cacheDir, tx, ty, tz, url);
    }


    private void savePng(String cacheDir, int tx, int ty, int tz, String url) {
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                synchronized (this) {
                    //得到从网上获取资源，转换成我们想要的类型
                    byte[] data = response.body().bytes();

                    try {
                        File parent = new File(cacheDir, String.valueOf(tz));
                        if (!parent.exists()) {
                            parent.mkdir();
                        }
                        FileImageOutputStream imageOutput = new FileImageOutputStream(new File(parent, String.format("%d-%d.%s", tx, ty, "png")));
                        imageOutput.write(data, 0, data.length);
                        imageOutput.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
}
