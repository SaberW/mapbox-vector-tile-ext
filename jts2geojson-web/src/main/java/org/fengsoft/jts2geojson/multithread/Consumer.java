package org.fengsoft.jts2geojson.multithread;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.beetl.sql.core.DSTransactionManager;
import org.beetl.sql.core.SQLManager;
import org.fengsoft.jts2geojson.common.TileIndex;
import org.fengsoft.jts2geojson.common.TileType;
import org.fengsoft.jts2geojson.entity.sqlite.ErrorTileInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class Consumer implements Runnable {
    private final GlobalMercator mercator;
    private final TileType tileType;
    private BlockingQueue<TileIndex> queue;
    private OkHttpClient okHttpClient;
    private final File subDir;
    private volatile boolean isRunning = true;
    private SQLManager sqlManager;
    private final Boolean isOverwrite;

    public Consumer(BlockingQueue<TileIndex> queue, SQLManager sqlManager, OkHttpClient okHttpClient, GlobalMercator mercator, String tileName, String imageTilePath, TileType tileType, Boolean isOverwrite) {
        this.queue = queue;
        this.okHttpClient = okHttpClient;
        this.mercator = mercator;
        this.tileType = tileType;
        this.sqlManager = sqlManager;
        this.isOverwrite = isOverwrite;

        File file = new File(imageTilePath);
        if (!file.exists()) file.mkdirs();
        subDir = new File(imageTilePath, tileName);
        if (!subDir.exists()) subDir.mkdir();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                TileIndex data = queue.take();
                if (data != null) {
                    generateTile(subDir.getAbsolutePath(), data.getX(), data.getY(), data.getZ(), tileType);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
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
            url = String.format(tileType.getUrl(), gootleXY[0] % 6, gootleXY[0], gootleXY[1], tz);
        } else if (tileType.getType().equals(TileType.TDTVEC.getType())) {
            url = String.format(tileType.getUrl(), gootleXY[0] % 6, gootleXY[0], gootleXY[1], tz);
        } else if (tileType.getType().equals(TileType.GOOGLEIMAGE.getType())) {
            url = String.format(tileType.getUrl(), gootleXY[0], gootleXY[1], tz);
        }
        if (!StringUtils.isEmpty(url))
            savePng(cacheDir, tx, ty, tz, url);
    }

    @Transactional
    public void savePng(String cacheDir, int tx, int ty, int tz, String url) {
        File parent = new File(cacheDir, String.valueOf(tz));
        if (!parent.exists()) parent.mkdir();
        File xFile = new File(parent, String.valueOf(tx));
        if (!xFile.exists()) xFile.mkdir();

        File targetFile = new File(xFile, String.format("%d.%s", ty, "png"));
        if (!isOverwrite && targetFile.exists()) {
            return;
        }

        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                DSTransactionManager.start();
                sqlManager.insert(ErrorTileInfo.class, new ErrorTileInfo(tx, ty, tz));
                try {
                    DSTransactionManager.commit();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                log.info(String.format("error tile:%d-%d-%d", tx, ty, tz));
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
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void stop() {
        isRunning = false;
    }
}