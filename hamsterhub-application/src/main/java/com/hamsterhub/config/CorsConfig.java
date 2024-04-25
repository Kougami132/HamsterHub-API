package com.hamsterhub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                //是否发送Cookie
//                .allowCredentials(true)
                //设置放行哪些原始域
                .allowedOrigins("*")
                //放行哪些请求方式
                .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS", "HEAD")
                //.allowedMethods("*") //或者放行全部
                //放行哪些原始请求头部信息
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
