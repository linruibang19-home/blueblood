package com.blueblood.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置：上传文件的静态资源映射。
 * 上传文件保存到本地目录，通过 /uploads/** 访问。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.base-path:./uploads}")
    private String uploadBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将磁盘上传目录映射到 /uploads/** 路径
        String location = new File(uploadBasePath).getAbsolutePath().replace("\\", "/")
                + (uploadBasePath.endsWith("/") ? "" : "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }
}
