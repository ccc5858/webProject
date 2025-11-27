package com.ccc.common.config;

import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RabbitConfig {

    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        List<String> allowedPatterns = Arrays.asList(
                "com.alibaba.fastjson.JSONArray",
                "com.alibaba.fastjson.JSONObject",
                "com.alibaba.fastjson2.JSONArray",  // 添加fastjson2支持
                "com.alibaba.fastjson2.JSONObject", // 添加fastjson2支持
                "java.util.*",
                "com.example.pojo.entity.*"  // 替换为实际的实体类包路径
        );
        converter.setAllowedListPatterns(allowedPatterns);
        return converter;
    }
}