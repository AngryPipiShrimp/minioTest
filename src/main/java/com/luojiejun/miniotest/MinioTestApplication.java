package com.luojiejun.miniotest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MinioTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinioTestApplication.class, args);
        System.out.println("\n 启动成功 \n");
    }

}
