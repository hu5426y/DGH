package com.example.dgh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.dgh.mapper")
public class DghApplication {
    public static void main(String[] args) {
        SpringApplication.run(DghApplication.class, args);
    }
}
