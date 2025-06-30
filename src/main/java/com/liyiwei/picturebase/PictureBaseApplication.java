package com.liyiwei.picturebase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.liyiwei.picturebase.mapper")
public class PictureBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureBaseApplication.class, args);
    }

}
