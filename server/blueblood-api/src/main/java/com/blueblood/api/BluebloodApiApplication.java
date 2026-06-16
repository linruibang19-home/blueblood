package com.blueblood.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 蓝血菁英平台后端 API 启动类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.blueblood.api")
@MapperScan("com.blueblood.api.modules.**.mapper")
public class BluebloodApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BluebloodApiApplication.class, args);
    }
}
