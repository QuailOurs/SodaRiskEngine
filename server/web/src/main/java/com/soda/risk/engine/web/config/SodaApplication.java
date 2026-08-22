package com.soda.risk.engine.web.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Soda 规则引擎服务入口。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.soda.risk.engine"})
@MapperScan(value = "com.soda.risk.engine.config", annotationClass = org.apache.ibatis.annotations.Mapper.class)
@EnableScheduling
public class SodaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodaApplication.class, args);
    }
}
