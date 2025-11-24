package com.ccc.service.listener;

import com.alibaba.fastjson.JSON;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.properties.JwtProperties;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.example.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
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

//    @RabbitListener(bindings =
//            @QueueBinding(
//                    exchange = @Exchange(value = "redis", durable = "true"),
//                    value = @Queue(value = "jwt", durable = "true"),
//                    key = "jwt"
//            )
//    )
//    public void listener(String message) {
//        try {
//            String[] split = message.split(":");
//            log.info("监听到消息：id:{}, token:{}", split[0], split[1]);
//            redisTemplate.opsForValue().set(RedisConstant.USER_TOKEN + split[0], split[1], jwtProperties.getTtl(), TimeUnit.MILLISECONDS);
//        } catch (Exception e) {
//            log.error("监听消息异常：{}", e.getMessage());
//        }
//    }

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "redis", durable = "true"),
                    value = @Queue(value = "user.redis.insert", durable = "true", arguments = {
                            @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                            @Argument(name = "x-dead-letter-exchange", value = "user.dead"),
                            @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                            @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                    }),
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

    @RabbitListener(bindings =
          @QueueBinding(
                  exchange = @Exchange(value = "redis", durable = "true"),
                  value = @Queue(value = "url.redis.insert", durable = "true", arguments = {
                          @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                          @Argument(name = "x-dead-letter-exchange", value = "url.dead"),
                          @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                          @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                  }),
                  key = "url"
          )
    )
    public void listenerUrl(String message) {
        if(message.length() < 4) {
            log.error("url消息异常");
            return;
        }
        try {
            String[] split = message.split("@");
            log.info("监听到url消息：{}", message);
            redisTemplate.opsForSet().add(RedisConstant.USER_UPLOAD + split[1], split[0]);
        } catch (Exception e) {
            log.error("监听url消息异常：{}", e.getMessage());
        }
    }

    @RabbitListener(bindings =
          @QueueBinding(
                  exchange = @Exchange(value = "redis", durable = "true"),
                  value = @Queue(value = "user.redis.delete", durable = "true", arguments = {
                          @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                          @Argument(name = "x-dead-letter-exchange", value = "url.dead"),
                          @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                          @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                  }),
                  key = "user"
          )
    )
    public void listenerUrlDelete(String message) {
        try {
            log.info("监听到user删除消息：{}", message);
            redisTemplate.delete(RedisConstant.USER_INFO + message);
        } catch (Exception e) {
            log.error("监听user删除消息异常：{}", e.getMessage());
        }
    }
}
