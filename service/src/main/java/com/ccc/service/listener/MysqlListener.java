package com.ccc.service.listener;

import com.alibaba.fastjson.JSON;
import com.ccc.service.mapper.CommonMapper;
import com.ccc.service.mapper.UserMapper;
import com.example.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MysqlListener {
//
//    @Autowired
//    private CommonMapper commonMapper;
//
//    @Autowired
//    private UserMapper userMapper;
//
//    @RabbitListener(bindings =
//          @QueueBinding(
//                  value = @Queue(value = "oss", durable = "true"),
//                  exchange = @Exchange(value = "mysql", durable = "true"),
//                  key = "upload"
//          )
//    )
//    public void listener(String message) {
//        String[] messages = message.split(":");
//        if(messages.length != 2) {
//            log.error("mysql: 参数错误");
//            return;
//        }
//        log.info("mysql: {}", messages[1]);
//        commonMapper.insert(messages[0], messages[1], LocalDateTime.now());
//    }
//
//    @RabbitListener(bindings =
//          @QueueBinding(
//                  value = @Queue(value = "user", durable = "true"),
//                  exchange = @Exchange(value = "mysql", durable = "true"),
//                  key = "user"
//          )
//    )
//    public void listenerUser(String message) {
//        log.info("mysql: {}", message);
//        User user = JSON.parseObject(message, User.class);
//        userMapper.insert(user);
//    }

}
