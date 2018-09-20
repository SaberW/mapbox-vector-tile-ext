package org.fengsoft.jts2geojson.services;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.beetl.sql.core.SQLManager;
import org.fengsoft.geojson.common.GlobalMercator;
import org.fengsoft.jts2geojson.common.TileIndex;
import org.fengsoft.jts2geojson.common.TileType;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
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

    private long totalCount = 0;// 总数 原子操作
    private AtomicInteger countVCRsuccess = new AtomicInteger();// 总数 原子操作
    private AtomicInteger countVECsuccess = new AtomicInteger();// 总数 原子操作

    private AtomicInteger countVCRerror = new AtomicInteger();// 总数 原子操作
    private AtomicInteger countVECerror = new AtomicInteger();// 总数 原子操作


    private volatile boolean isRunning = true;
    private volatile boolean isOverwrite = false;
    private volatile TileType tileType;
    private volatile String tileName;
    private volatile boolean isOver = false;

    private volatile BlockingQueue<TileIndex> queue = new LinkedBlockingDeque<>(30);// 内存缓冲区
    private Envelope envelope;

    public AtomicInteger getCountVCRsuccess() {
        return countVCRsuccess;
    }

    public AtomicInteger getCountVECsuccess() {
        return countVECsuccess;
    }

    public AtomicInteger getCountVCRerror() {
        return countVCRerror;
    }

    public AtomicInteger getCountVECerror() {
        return countVECerror;
    }

    public void run(String tileName, Envelope envelope, String epsg, TileType tileType, Boolean isOverwrite) {
        this.tileName = tileName;
        this.envelope = envelope;
        this.tileType = tileType;
        this.isOverwrite = isOverwrite;
        totalCount = 0;

        if (!new File(imageTilePath).exists()) new File(imageTilePath).mkdirs();
        if (!new File(imageTilePath, tileName).exists()) new File(imageTilePath, tileName).mkdirs();

        if (tileType.getType().equals(TileType.TDT.getType())) {
            if (!new File(new File(imageTilePath, tileName), TileType.TDTCVR.getType()).exists())
                new File(new File(imageTilePath, tileName), TileType.TDTCVR.getType()).mkdir();
            if (!new File(new File(imageTilePath, tileName), TileType.TDTVEC.getType()).exists())
                new File(new File(imageTilePath, tileName), TileType.TDTVEC.getType()).mkdir();
        }


        double[] min = mercator.latLonToMeters(envelope.getMinY(), envelope.getMinX());
        double[] max = mercator.latLonToMeters(envelope.getMaxY(), envelope.getMaxX());

        ExecutorService service = Executors.newFixedThreadPool(threadSize);
        service.execute(() -> {
            for (int tz = tmaxz; tz > tminz - 1; tz--) {
                if (tileType.getType().equals(TileType.TDT.getType())) {
                    File parentCVR = new File(imageTilePath + File.separator + tileName + File.separator + TileType.TDTCVR.getType(), String.valueOf(tz));
                    File parentVEC = new File(imageTilePath + File.separator + tileName + File.separator + TileType.TDTVEC.getType(), String.valueOf(tz));
                    if (!parentCVR.exists()) parentCVR.mkdir();
                    if (!parentVEC.exists()) parentVEC.mkdir();
                } else {
                    File file = new File(imageTilePath + File.separator + tileName, String.valueOf(tz));
                    if (!file.exists()) file.mkdir();
                }

                int[] tminxy = this.mercator.metersToTile(min[0], min[1], tz);
                int[] tmaxxy = this.mercator.metersToTile(max[0], max[1], tz);
                tminxy = new int[]{Math.max(0, tminxy[0]), Math.max(0, tminxy[1])};
                tmaxxy = new int[]{(int) Math.min(Math.pow(2, tz) - 1, tmaxxy[0]), (int) Math.min(Math.pow(2, tz) - 1, tmaxxy[1])};
                for (int tx = tminxy[0]; tx < tmaxxy[0] + 1; tx++) {
                    if (tileType.getType().equals(TileType.TDT.getType())) {
                        File xFileCVR = new File(imageTilePath + File.separator + tileName + File.separator + TileType.TDTCVR.getType() + File.separator + String.valueOf(tz), String.valueOf(tx));
                        if (!xFileCVR.exists()) xFileCVR.mkdir();
                        File xFileVEC = new File(imageTilePath + File.separator + tileName + File.separator + TileType.TDTVEC.getType() + File.separator + String.valueOf(tz), String.valueOf(tx));
                        if (!xFileVEC.exists()) xFileVEC.mkdir();
                    } else {
                        File file = new File(imageTilePath + File.separator + tileName + File.separator + String.valueOf(tz), String.valueOf(tx));
                        if (!file.exists()) file.mkdir();
                    }


                    for (int ty = tmaxxy[1]; ty > tminxy[1] - 1; ty--) {
                        if (isRunning) {
                            try {
                                queue.put(new TileIndex(tx, ty, tz));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            isOver = true;
        });

        for (int i = 0; i < threadSize; i++) {
            service.execute(() -> download());
        }
    }

    /**
     * //                if (tileType.getType().equals(TileType.BING.getType())) {
     * //                    String code = mercator.tileXYToQuadKey(gootleXY[0], gootleXY[1], tileIndex.getZ());
     * //                    url = String.format(tileType.getUrl(), code);
     * //                } else if (tileType.getType().equals(TileType.GOOGLE.getType())) {
     * //                    url = String.format(tileType.getUrl(), tileIndex.getZ(), gootleXY[0], gootleXY[1]);
     * //                } else if (tileType.getType().equals(TileType.OSM.getType())) {
     * //                    url = String.format(tileType.getUrl(), tileIndex.getZ(), tileIndex.getX(), tileIndex.getY());
     * //                } else if (tileType.getType().equals(TileType.GOOGLEIMAGE.getType())) {
     * //                    url = String.format(tileType.getUrl(), gootleXY[0], gootleXY[1], tileIndex.getZ());
     */
    public void download() {
        while (true) {
            TileIndex tileIndex = queue.poll();
            if (tileIndex == null && isOver) {
                break;
            }
            if (tileIndex != null) {
                if (tileType.getType().equals(TileType.TDT.getType())) {
                    File targetCVR = new File(new File(imageTilePath, tileName + File.separator + TileType.TDTCVR.getType() + File.separator + tileIndex.getZ() + File.separator + tileIndex.getX()), String.format("%d.%s", tileIndex.getY(), "png"));
                    File targetVEC = new File(new File(imageTilePath, tileName + File.separator + TileType.TDTVEC.getType() + File.separator + tileIndex.getZ() + File.separator + tileIndex.getX()), String.format("%d.%s", tileIndex.getY(), "png"));

                    if (isOverwrite || !targetCVR.exists()) {
                        generateTile(TileType.TDTCVR, tileIndex, targetCVR);
                    } else {
                        countVCRsuccess.getAndIncrement();
                    }
                    if (isOverwrite || !targetVEC.exists()) {
                        generateTile(TileType.TDTVEC, tileIndex, targetVEC);
                    } else {
                        countVECsuccess.getAndIncrement();
                    }
                } else {
                    File target = new File(new File(imageTilePath, tileName + File.separator + tileIndex.getZ() + File.separator + tileIndex.getX()), String.format("%d.%s", tileIndex.getY(), "png"));
                    if (isOverwrite || !target.exists()) {
                        generateTile(tileType, tileIndex, target);
                    } else {
                        countVCRsuccess.getAndIncrement();
                    }
                }
            }
        }
    }

    private void generateTile(TileType tileType, TileIndex tileIndex, File targetFile) {
        int[] gootleXY = mercator.googleTile(tileIndex.getX(), tileIndex.getY(), tileIndex.getZ());
        String url = String.format(tileType.getUrl(), gootleXY[0] % 6, gootleXY[0], gootleXY[1], tileIndex.getZ());
        Request request = new Request.Builder().url(url).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.info(String.format("error tile:%d-%d-%d：%s", tileIndex.getX(), tileIndex.getY(), tileIndex.getZ(), e.getMessage()));
                if (tileType.getType().equals(TileType.TDTCVR)) {
                    countVCRerror.getAndIncrement();
                } else if (tileType.getType().equals(TileType.TDTVEC))
                    countVECerror.getAndIncrement();
                else countVCRerror.getAndIncrement();
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
                        if (tileType.getType().equals(TileType.TDTCVR)) {
                            countVCRsuccess.getAndIncrement();
                        } else if (tileType.getType().equals(TileType.TDTVEC))
                            countVECsuccess.getAndIncrement();
                        else countVCRsuccess.getAndIncrement();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }


    public long getTotalCount(TileType tileType) {
        if (totalCount == 0) {
            double[] min = mercator.latLonToMeters(envelope.getMinY(), envelope.getMinX());
            double[] max = mercator.latLonToMeters(envelope.getMaxY(), envelope.getMaxX());
            for (int tz = tmaxz; tz > tminz - 1; tz--) {
                int[] tminxy = this.mercator.metersToTile(min[0], min[1], tz);
                int[] tmaxxy = this.mercator.metersToTile(max[0], max[1], tz);

                tminxy = new int[]{Math.max(0, tminxy[0]), Math.max(0, tminxy[1])};
                tmaxxy = new int[]{(int) Math.min(Math.pow(2, tz) - 1, tmaxxy[0]), (int) Math.min(Math.pow(2, tz) - 1, tmaxxy[1])};

                totalCount += (tmaxxy[1] - (tminxy[1] - 1)) * ((tmaxxy[0] + 1) - tminxy[0]);
            }
            if (tileType.getType().equals(TileType.TDT)) {
                return totalCount * 2;
            } else
                return totalCount;
        } else return totalCount;
    }
}
