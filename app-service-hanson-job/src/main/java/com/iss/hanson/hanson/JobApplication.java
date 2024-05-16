package com.iss.hanson.hanson;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hanson
 * @date 2021/11/18  17:49
 */
@SpringBootApplication
@MapperScan(basePackages = "com.iss.hanson.hanson.dao.mappers.")
public class JobApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}
