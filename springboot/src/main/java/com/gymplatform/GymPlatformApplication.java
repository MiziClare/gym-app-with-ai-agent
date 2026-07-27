package com.gymplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gymplatform.mapper")
public class GymPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymPlatformApplication.class, args);
    }
}
