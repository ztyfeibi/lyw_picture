package com.liyiwei.picturebase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@MapperScan("com.liyiwei.picturebase.mapper")
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PictureBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureBaseApplication.class, args);
    }

}

/*
TODO
 1. 缓存击穿：某些热点数据过期，大量请求直接打到数据库
 A: 设置热点数据的过期时间延长，或使用互斥锁（Redisson）控制缓存刷新
TODO
 2. 缓存穿透： 用户频繁请求不存在的数据，导致大量请求被发到数据库
 A: 对无效查询也缓存（如空值缓存），或使用布隆过滤器
TODO
 3. 缓存雪崩：大量缓存同时过期，大量请求打到数据库
 A: 设置缓存的不同过期时间，避免同时过期，或者使用多级缓存，减少对数据库的依赖

 TODO
  自动识别热点图片，进行缓存
  采用热key探测技术，实时对图片的访问量进行统计，自动将热点图片添加到缓存。

 TODO
  查询优化：只查询必要字段
  图片压缩
  缩略图
  图片删除（新接口，重新上传图片时）
  获取图片主色调，实现颜色搜图，欧几里得算法计算颜色相似度

 todo
  各个方法总结 colutil、strutil
 */