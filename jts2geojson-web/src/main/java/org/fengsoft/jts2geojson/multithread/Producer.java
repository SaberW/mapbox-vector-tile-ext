package org.fengsoft.jts2geojson.multithread;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
import org.fengsoft.jts2geojson.common.TileIndex;
import org.locationtech.jts.geom.Envelope;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Producer implements Runnable {
    private volatile boolean isRunning = true;
    private BlockingQueue<TileIndex> queue;// 内存缓冲区
    private static AtomicInteger count = new AtomicInteger();// 总数 原子操作
    private final Envelope envelope;
    private final GlobalMercator mercator;

    private int tminz = 1;
    private int tmaxz = 18;


    public Producer(BlockingQueue<TileIndex> queue, Envelope envelope,int tminz,int tmaxz) {
        this.queue = queue;
        this.envelope = envelope;
        this.tminz=tminz;
        this.tmaxz = tmaxz;
        this.mercator = new GlobalMercator(256);
    }

    @Override
    public void run() {
        double[] min = mercator.latLonToMeters(envelope.getMinY(), envelope.getMinX());
        double[] max = mercator.latLonToMeters(envelope.getMaxY(), envelope.getMaxX());

        double ominx = min[0];
        double omaxx = max[0];
        double omaxy = max[1];
        double ominy = min[1];

        LinkedList<int[]>   tminmax = new LinkedList<>();
        for (int tz = 0; tz < 32; tz++) {
            int[] tminxy = this.mercator.metersToTile(ominx, ominy, tz);
            int[] tmaxxy = this.mercator.metersToTile(omaxx, omaxy, tz);

            tminxy = new int[]{Math.max(0, tminxy[0]), Math.max(0, tminxy[1])};
            tmaxxy = new int[]{(int) Math.min(Math.pow(2, tz) - 1, tmaxxy[0]), (int) Math.min(Math.pow(2, tz) - 1, tmaxxy[1])};

            tminmax.add(tz, new int[]{tminxy[0], tminxy[1], tmaxxy[0], tmaxxy[1]});
        }

        int tminx = tminmax.get(tmaxz)[0];
        int tminy = tminmax.get(tmaxz)[1];
        int tmaxx = tminmax.get(tmaxz)[2];
        int tmaxy = tminmax.get(tmaxz)[3];

        int tz = tmaxz;

        for (int ty = tmaxy; ty > tminy - 1; ty--) {
            for (int tx = tminx; tx < tmaxx + 1; tx++) {
                count.getAndIncrement();
                if (isRunning) {
                    try {
                        queue.put(new TileIndex(tx, ty, tz));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        for (tz = tmaxz - 1; tz > tminz - 1; tz--) {
            int[] tminxytmaxxy = tminmax.get(tz);
            for (int ty = tminxytmaxxy[3]; ty > tminxytmaxxy[1] - 1; ty--) {
                for (int tx = tminxytmaxxy[0]; tx < tminxytmaxxy[2] + 1; tx++) {
                    count.getAndIncrement();
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
    }

    public void stop() {
        isRunning = false;
    }
}