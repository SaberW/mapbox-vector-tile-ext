package org.fengsoft.jts2geojson.config;

import com.zaxxer.hikari.HikariDataSource;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.beetl.ext.spring.BeetlSpringViewResolver;
import org.beetl.sql.core.ClasspathLoader;
import org.beetl.sql.core.Interceptor;
import org.beetl.sql.core.NameConversion;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.ext.DebugInterceptor;
import org.beetl.sql.ext.spring4.BeetlSqlDataSource;
import org.beetl.sql.ext.spring4.BeetlSqlScannerConfigurer;
import org.beetl.sql.ext.spring4.SqlManagerFactoryBean;
import org.fengsoft.jts2geojson.Application;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @Author JerFer
 * @Date 2018/8/13---10:27
 */
@Configuration
public class BeetlConfig {
    @Bean
    public BeetlGroupUtilConfiguration getBeetlGroupUtilConfiguration(Environment env) {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = new BeetlGroupUtilConfiguration();
        //获取Spring Boot 的ClassLoader
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = BeetlConfig.class.getClassLoader();
        }
        ClasspathResourceLoader cploder = new ClasspathResourceLoader(loader, env.getProperty("beetl.templatesPath"));
        beetlGroupUtilConfiguration.setResourceLoader(cploder);
        beetlGroupUtilConfiguration.init();
        //如果使用了优化编译器，涉及到字节码操作，需要添加ClassLoader
        beetlGroupUtilConfiguration.getGroupTemplate().setClassLoader(loader);
        return beetlGroupUtilConfiguration;

    }

    @Bean(name = "beetlViewResolver")
    public BeetlSpringViewResolver getBeetlSpringViewResolver(BeetlGroupUtilConfiguration beetlGroupUtilConfiguration) {
        BeetlSpringViewResolver beetlSpringViewResolver = new BeetlSpringViewResolver();
        beetlSpringViewResolver.setContentType("text/html;charset=UTF-8");
        beetlSpringViewResolver.setOrder(0);
        beetlSpringViewResolver.setSuffix(".html");
        beetlSpringViewResolver.setConfig(beetlGroupUtilConfiguration);
        return beetlSpringViewResolver;
    }

    @Bean(name = "beetlSqlScannerConfigurerPG")
    public BeetlSqlScannerConfigurer getBeetlSqlScannerConfigurerPG(Environment env) {
        BeetlSqlScannerConfigurer conf = new BeetlSqlScannerConfigurer();
        conf.setBasePackage(env.getProperty("beetlsql.pg.basePackage"));
        conf.setDaoSuffix(env.getProperty("beetlsql.pg.daoSuffix"));
        conf.setSqlManagerFactoryBeanName("sqlManagerFactoryBeanPG");
        return conf;
    }

    @Bean(name = "beetlSqlScannerConfigurerSqlite")
    public BeetlSqlScannerConfigurer getBeetlSqlScannerConfigurerSqlite(Environment env) {
        BeetlSqlScannerConfigurer conf = new BeetlSqlScannerConfigurer();
        conf.setBasePackage(env.getProperty("beetlsql.sqlite.basePackage"));
        conf.setDaoSuffix(env.getProperty("beetlsql.sqlite.daoSuffix"));
        conf.setSqlManagerFactoryBeanName("sqlManagerFactoryBeanSqlite");
        return conf;
    }

    @Bean(name = "sqlManagerFactoryBeanPG")
    public SqlManagerFactoryBean getSqlManagerFactoryBeanPG(@Qualifier("pg") DataSource datasource, Environment env) {
        SqlManagerFactoryBean factory = new SqlManagerFactoryBean();

        BeetlSqlDataSource source = new BeetlSqlDataSource();
        source.setMasterSource(datasource);
        factory.setCs(source);
        try {
            factory.setDbStyle((DBStyle) (Application.class.getClassLoader()).loadClass(env.getProperty("beetlsql.pg.dbStyle")).newInstance());
            factory.setNc((NameConversion) (Application.class.getClassLoader().loadClass(env.getProperty("beetlsql.pg.nameConversion")).newInstance()));//开启驼峰
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        Boolean isDev = Boolean.parseBoolean(env.getProperty("beetl-beetlsq", "dev=false").split("=")[1]);
        factory.setInterceptors(isDev ? new Interceptor[]{new DebugInterceptor()} : new Interceptor[]{});

        factory.setSqlLoader(new ClasspathLoader(env.getProperty("beetlsql.pg.sqlPath")));//sql文件路径
        return factory;
    }

    @Bean(name = "sqlManagerFactoryBeanSqlite")
    public SqlManagerFactoryBean getSqlManagerFactoryBean(@Qualifier("sqlite") DataSource datasource, Environment env) {
        SqlManagerFactoryBean factory = new SqlManagerFactoryBean();

        BeetlSqlDataSource source = new BeetlSqlDataSource();
        source.setMasterSource(datasource);
        factory.setCs(source);
        try {
            factory.setDbStyle((DBStyle) (Application.class.getClassLoader()).loadClass(env.getProperty("beetlsql.sqlite.dbStyle")).newInstance());
            factory.setNc((NameConversion) (Application.class.getClassLoader().loadClass(env.getProperty("beetlsql.sqlite.nameConversion")).newInstance()));//开启驼峰
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        Boolean isDev = Boolean.parseBoolean(env.getProperty("beetl-beetlsq", "dev=false").split("=")[1]);
        factory.setInterceptors(isDev ? new Interceptor[]{new DebugInterceptor()} : new Interceptor[]{});

        factory.setSqlLoader(new ClasspathLoader(env.getProperty("beetlsql.sqlite.sqlPath")));//sql文件路径
        return factory;
    }

    @Bean("pg")
    public DataSource datasourcePG(Environment env) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env.getProperty("spring.datasource.pg.url"));
        ds.setUsername(env.getProperty("spring.datasource.pg.username"));
        ds.setPassword(env.getProperty("spring.datasource.pg.password"));
        ds.setDriverClassName(env.getProperty("spring.datasource.pg.driver-class-name"));

        ds.setConnectionTimeout(Long.parseLong(env.getProperty("spring.datasource.pg.hikari.connection-timeout")));
        ds.setIdleTimeout(Long.parseLong(env.getProperty("spring.datasource.pg.hikari.idle-timeout")));
        ds.setMaxLifetime(Long.parseLong(env.getProperty("spring.datasource.pg.hikari.max-lifetime")));
        ds.setMaximumPoolSize(Integer.parseInt(env.getProperty("spring.datasource.pg.hikari.maximum-pool-size")));
        ds.setMinimumIdle(Integer.parseInt(env.getProperty("spring.datasource.pg.hikari.minimum-idle")));
        return ds;
    }

    @Bean("sqlite")
    public DataSource datasourceSqlite(Environment env) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env.getProperty("spring.datasource.sqlite.url"));
        ds.setUsername(env.getProperty("spring.datasource.sqlite.username"));
        ds.setPassword(env.getProperty("spring.datasource.sqlite.password"));
        ds.setDriverClassName(env.getProperty("spring.datasource.sqlite.driver-class-name"));

        ds.setConnectionTimeout(Long.parseLong(env.getProperty("spring.datasource.sqlite.hikari.connection-timeout")));
        ds.setIdleTimeout(Long.parseLong(env.getProperty("spring.datasource.sqlite.hikari.idle-timeout")));
        ds.setMaxLifetime(Long.parseLong(env.getProperty("spring.datasource.sqlite.hikari.max-lifetime")));
        ds.setMaximumPoolSize(Integer.parseInt(env.getProperty("spring.datasource.sqlite.hikari.maximum-pool-size")));
        ds.setMinimumIdle(Integer.parseInt(env.getProperty("spring.datasource.sqlite.hikari.minimum-idle")));
        return ds;
    }
}
