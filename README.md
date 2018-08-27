#### 基于MapBox的vectorTile标准及实现类，实现矢量切片，生成和加载

* 目前只实现了基于4326的切片
* 目前没有实现多线程切图功能
* 其中根据切片计算bbox的参考我的另外一个项目
    > 项目地址：[gdal2tiles](https://github.com/polixiaohai/gdal2tiles)
    
    
#### 实现天地图、bing地图，OSM地图，Google地图的切片下载及加载实例

* 目前只实现离散文件形式保存，后续添加mbtiles（SQLite）和其他数据库保存方式。
* 其中bing地图（国内），Google地图（国内）都是有偏移的，天地图（新版）没有偏移，但参考系统为2000
    > 可以自己百度一把，2000和84的区别，可得出结论，几乎是亚毫米级的，所以可认为一致。
* 其中根据切片计算bbox的参考我的另外一个项目
    > 项目地址：[gdal2tiles](https://github.com/polixiaohai/gdal2tiles)
