package com.ccc.common.config;


import com.ccc.common.interceptor.LoginInterceptor;
import com.ccc.common.interceptor.RefreshInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig extends WebMvcConfigurationSupport {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private RefreshInterceptor refreshInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/user/login", "/user/register").order(1);
        registry.addInterceptor(refreshInterceptor).addPathPatterns("/**").order(0);
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("POST", "PUT", "GET", "DELETE", "OPTIONS")
                .allowedOrigins("*");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // 允许所有路径
                        .allowedOrigins("*")  // 允许所有来源
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                        .allowedHeaders("*")  // 允许的请求头
                        .allowCredentials(true)  // 允许发送凭证
                        .maxAge(3600);  // CORS预检请求的缓存时间
            }
        };
    }
}
