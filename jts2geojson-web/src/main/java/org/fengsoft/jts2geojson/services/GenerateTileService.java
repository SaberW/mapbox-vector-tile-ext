package org.fengsoft.jts2geojson.services;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
import okhttp3.*;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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


    public void run(Envelope envelope, String epsg) {

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
                int[] gootleXY = mercator.googleTile(tx, ty, tz);
                String code = mercator.tileXYToQuadKey(gootleXY[0], gootleXY[1], tz);
                savePng(code, gootleXY[0], gootleXY[1], tz);
            }
        }

        for (tz = tmaxz - 1; tz > tminz - 1; tz--) {
            int[] tminxytmaxxy = tminmax.get(tz);
            for (int ty = tminxytmaxxy[3]; ty > tminxytmaxxy[1] - 1; ty--) {
                for (int tx = tminxytmaxxy[0]; tx < tminxytmaxxy[2] + 1; tx++) {
                    int[] gootleXY = mercator.googleTile(tx, ty, tz);
                    String code = mercator.tileXYToQuadKey(gootleXY[0], gootleXY[1], tz);
                    savePng(code, gootleXY[0], gootleXY[1], tz);
                }
            }
        }
    }

    private void savePng(String code, int tx, int ty, int tz) {
        String bingUrl = "https://dynamic.t0.tiles.ditu.live.com/comp/ch/" + code + "?mkt=zh-CN&ur=cn&it=G,TW,BX,L&cstl=w4c";
        String googleUrl = "http://www.google.cn/maps/vt/pb=!1m4!1m3!1i" + tz + "!2i" + tx + "!3i" + ty + "!2m3!1e0!2sm!3i380072576!3m8!2szh-CN!3scn!5e1105!12m4!1e68!2m2!1sset!2sRoadmap!4e0!5m1!1e0";
        Request request = new Request.Builder().url(googleUrl).build();
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
                        File parent = new File(imageTilePath, String.valueOf(tz));
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

    private void savePng(String code, int tz) {
        Request request = new Request.Builder().url("https://dynamic.t0.tiles.ditu.live.com/comp/ch/" + code + "?mkt=zh-CN&ur=cn&it=G,TW,BX,L&cstl=w4c").build();
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
                        File parent = new File(imageTilePath, String.valueOf(tz));
                        if (!parent.exists()) {
                            parent.mkdir();
                        }
                        FileImageOutputStream imageOutput = new FileImageOutputStream(new File(parent, String.format("%s.%s", code, "png")));
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
