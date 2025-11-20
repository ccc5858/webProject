package com.ccc.service.listener;

import com.alibaba.fastjson.JSON;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.properties.JwtProperties;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.example.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtProperties jwtProperties;

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "redis", durable = "true"),
                    value = @Queue(value = "jwt", durable = "true"),
                    key = "jwt"
            )
    )
    public void listener(String message) {
        try {
            String[] split = message.split(":");
            log.info("监听到消息：id:{}, token:{}", split[0], split[1]);
            redisTemplate.opsForValue().set(RedisConstant.USER_TOKEN + split[0], split[1], jwtProperties.getTtl(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("监听消息异常：{}", e.getMessage());
        }
    }

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "redis", durable = "true"),
                    value = @Queue(value = "user", durable = "true"),
                    key = "user"
            )
    )
    public void listenerUser(String message) {
        try {
            log.info("监听到用户消息：{}", message);
            User user = JSON.parseObject(message, User.class);
            redisTemplate.opsForValue().set(RedisConstant.USER_INFO + user.getId(), message);
        } catch (Exception e) {
            log.error("监听用户消息异常：{}", e.getMessage());
        }
    }
}
