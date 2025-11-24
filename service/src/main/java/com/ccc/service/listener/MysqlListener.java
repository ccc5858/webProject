package com.ccc.service.listener;

import com.alibaba.fastjson.JSON;
import com.ccc.service.mapper.CommonMapper;
import com.ccc.service.mapper.UserMapper;
import com.example.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MysqlListener {

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private UserMapper userMapper;

    @RabbitListener(bindings =
          @QueueBinding(
                  value = @Queue(value = "user.mysql.insert", durable = "true", arguments = {
                          @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                          @Argument(name = "x-dead-letter-exchange", value = "user.dead"),
                          @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                          @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                  }),
                  exchange = @Exchange(value = "mysql", durable = "true"),
                  key = "user"
          )
    )
    public void listenerUser(String message) {
        log.info("mysql: {}", message);
        User user = JSON.parseObject(message, User.class);
        userMapper.insert(user);
    }

    @RabbitListener(bindings =
          @QueueBinding(
                  value = @Queue(value = "url.mysql.insert", durable = "true", arguments = {
                          @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                          @Argument(name = "x-dead-letter-exchange", value = "url.dead"),
                          @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                          @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                  }),
                  exchange = @Exchange(value = "mysql", durable = "true"),
                  key = "url"
          )
    )
    public void listenerUrl(String message) {
        log.info("mysql: {}", message);
        String[] messages = message.split("@");
        commonMapper.insert(Integer.parseInt(messages[1]), messages[0], LocalDateTime.now(), messages[2], messages[3]);
    }

    @RabbitListener(bindings =
         @QueueBinding(
                 value = @Queue(value = "user.mysql.delete", durable = "true", arguments = {
                         @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                         @Argument(name = "x-dead-letter-exchange", value = "user.dead"),
                         @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                         @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                 }),
                 exchange = @Exchange(value = "mysql", durable = "true"),
                 key = "user"
         )
    )
    public void listenerUserDelete(String message) {
        log.info("mysql: {}", message);
        userMapper.delete(Integer.parseInt(message));
    }



}
