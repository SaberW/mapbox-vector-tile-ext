package org.fengsoft.jts2geojson.services;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.beetl.sql.core.SQLManager;
import org.fengsoft.jts2geojson.common.TileType;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class GenerateTileService {
    @Autowired
    private GlobalMercator mercator;
    @Autowired
    @Qualifier("okHttpClient")
    private OkHttpClient okHttpClient;
    @Value("${cache.image-tile-path}")
    private String imageTilePath;
    @Value("${cache.maxz}")
    private Integer tmaxz;
    @Value("${cache.minz}")
    private Integer tminz;
    @Value("${thread.size}")
    private Integer threadSize;
    @Autowired
    @Qualifier("sqlManagerFactoryBeanSqlite")
    private SQLManager sqlManager;
    private AtomicInteger count = new AtomicInteger();// 总数 原子操作
    private AtomicInteger failCount = new AtomicInteger();// 总数 原子操作
    private volatile boolean isRunning = true;
    private Envelope envelope;

    public void run(String tileName, Envelope envelope, String epsg, TileType tileType, Boolean isOverwrite) {
        this.envelope = envelope;
        File file = new File(imageTilePath);
        if (!file.exists()) file.mkdirs();
        File subDir = new File(imageTilePath, tileName);
        if (!subDir.exists()) subDir.mkdir();
        double[] min = mercator.latLonToMeters(envelope.getMinY(), envelope.getMinX());
        double[] max = mercator.latLonToMeters(envelope.getMaxY(), envelope.getMaxX());
        for (int tz = tmaxz; tz > tminz - 1; tz--) {
            File parent = new File(subDir, String.valueOf(tz));
            if (!parent.exists()) parent.mkdir();
            int[] tminxy = this.mercator.metersToTile(min[0], min[1], tz);
            int[] tmaxxy = this.mercator.metersToTile(max[0], max[1], tz);
            tminxy = new int[]{Math.max(0, tminxy[0]), Math.max(0, tminxy[1])};
            tmaxxy = new int[]{(int) Math.min(Math.pow(2, tz) - 1, tmaxxy[0]), (int) Math.min(Math.pow(2, tz) - 1, tmaxxy[1])};
            for (int tx = tminxy[0]; tx < tmaxxy[0] + 1; tx++) {
                File xFile = new File(parent, String.valueOf(tx));
                if (!xFile.exists()) xFile.mkdir();
                for (int ty = tmaxxy[1]; ty > tminxy[1] - 1; ty--) {
                    File targetFile = new File(xFile, String.format("%d.%s", ty, "png"));
                    if (isRunning) {
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
                            url = String.format(tileType.getUrl(), gootleXY[0] % 6, gootleXY[0], gootleXY[1], tz);
                        } else if (tileType.getType().equals(TileType.TDTVEC.getType())) {
                            url = String.format(tileType.getUrl(), gootleXY[0] % 6, gootleXY[0], gootleXY[1], tz);
                        } else if (tileType.getType().equals(TileType.GOOGLEIMAGE.getType())) {
                            url = String.format(tileType.getUrl(), gootleXY[0], gootleXY[1], tz);
                        }
                        if (!StringUtils.isEmpty(url)) {
                            if (!isOverwrite && targetFile.exists()) {
                                return;
                            }
                            Request request = new Request.Builder().url(url).build();

                            Call call = okHttpClient.newCall(request);
                            int finalTx = tx;
                            int finalTy = ty;
                            int finalTz = tz;
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    log.info(String.format("error tile:%d-%d-%d：%s", finalTx, finalTy, finalTz, e.getMessage()));
                                    failCount.getAndIncrement();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    synchronized (this) {
                                        //得到从网上获取资源，转换成我们想要的类型
                                        byte[] data = response.body().bytes();

                                        try {
                                            FileImageOutputStream imageOutput = new FileImageOutputStream(targetFile);
                                            imageOutput.write(data, 0, data.length);
                                            imageOutput.close();
                                            count.getAndIncrement();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            });

                        }
                    }
                }
            }
        }
    }

    public int getTotalCount() {
        double[] min = mercator.latLonToMeters(envelope.getMinY(), envelope.getMinX());
        double[] max = mercator.latLonToMeters(envelope.getMaxY(), envelope.getMaxX());

        int totalCount = 0;
        for (int tz = tmaxz; tz > tminz - 1; tz--) {
            int[] tminxy = this.mercator.metersToTile(min[0], min[1], tz);
            int[] tmaxxy = this.mercator.metersToTile(max[0], max[1], tz);

            tminxy = new int[]{Math.max(0, tminxy[0]), Math.max(0, tminxy[1])};
            tmaxxy = new int[]{(int) Math.min(Math.pow(2, tz) - 1, tmaxxy[0]), (int) Math.min(Math.pow(2, tz) - 1, tmaxxy[1])};

            totalCount += (tmaxxy[1] - (tminxy[1] - 1)) * ((tmaxxy[0] + 1) - tminxy[0]);
        }
        return totalCount;
    }
}
