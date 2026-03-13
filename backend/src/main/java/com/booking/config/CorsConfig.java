package com.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 全局 CORS 配置，允许携带凭证（Cookies/Session），并使用 Origin Patterns 匹配本地开发端口。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 【核心配置】：使用 Origin Patterns 匹配本地的任意端口，解决 "*"+"allowCredentials=true" 的冲突
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*", 
            "http://127.0.0.1:*"
            // 生产环境应该配置为实际域名，如 "https://your-domain.com"
        ));
        
        // 允许所有必要的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许所有请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 【必须】：允许携带 Cookies/Session 凭证
        configuration.setAllowCredentials(true); 
        
        // 设置缓存时间，优化性能
        configuration.setMaxAge(3600L);
        
        // 将配置应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}