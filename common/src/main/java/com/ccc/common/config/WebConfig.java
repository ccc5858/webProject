package com.ccc.common.config;

//import com.ccc.common.interceptor.RefreshInterceptor;
//import com.ccc.common.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

//    @Autowired
//    private RefreshInterceptor refreshInterceptor;
//
//    @Autowired
//    private LoginInterceptor LoginInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(refreshInterceptor).addPathPatterns("/**").order(0);
//        registry.addInterceptor(LoginInterceptor).addPathPatterns("/**").excludePathPatterns("/user/login").order(1);
    }
}
