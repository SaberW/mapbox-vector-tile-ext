package org.fengsoft.jts2geojson.services;

import cn.com.enersun.dgpmicro.common.GlobalMercator;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.beetl.sql.core.SQLManager;
import org.fengsoft.jts2geojson.common.TileIndex;
import org.fengsoft.jts2geojson.common.TileType;
import org.fengsoft.jts2geojson.multithread.Consumer;
import org.fengsoft.jts2geojson.multithread.Producer;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.*;

@Service
@Slf4j
public class GenerateTileService {
    @Autowired
    private GlobalMercator mercator;
    @Value("${cache.image-tile-path}")
    private String imageTilePath;
    @Value("${thread.size}")
    private Integer threadSize;
    @Value("${thread.http.pool}")
    private Integer httpPoolSize;
    @Autowired
    @Qualifier("sqlManagerFactoryBeanSqlite")
    private SQLManager sqlManager;


    public void run(String tileName, Envelope envelope, String epsg, TileType tileType,Boolean isOverwrite) {
        BlockingQueue<TileIndex> queue = new LinkedBlockingDeque<>(100);
        Producer p1 = new Producer(queue, envelope, 1, 18);
        ExecutorService service = Executors.newFixedThreadPool(threadSize);
        service.execute(p1);
        for (int i = 0; i < threadSize; i++) {
            service.execute(new Consumer(queue, sqlManager, okHttpClient(), mercator, tileName, imageTilePath, tileType,isOverwrite));
        }
    }

    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    public SSLSocketFactory sslSocketFactory() {
        try {
            //信任任何链接
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new connection pool with tuning parameters appropriate for a single-user application.
     * The tuning parameters in this pool are subject to change in future OkHttp releases. Currently
     */
    public ConnectionPool pool() {
        return new ConnectionPool(httpPoolSize, 5, TimeUnit.MINUTES);
    }

    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory(), x509TrustManager())
                .retryOnConnectionFailure(false)//是否开启缓存
                .connectionPool(pool())//连接池
                .connectTimeout(20L, TimeUnit.SECONDS)
                .readTimeout(20L, TimeUnit.SECONDS)
                .build();
    }
}
