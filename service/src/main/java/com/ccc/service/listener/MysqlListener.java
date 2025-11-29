package com.ccc.service.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.ccc.service.mapper.CommentMapper;
import com.ccc.service.mapper.UrlMapper;
import com.ccc.service.mapper.UserMapper;
import com.example.pojo.entity.Comment;
import com.example.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class MysqlListener {

    @Autowired
    private UrlMapper urlMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @RabbitListener(bindings =
          @QueueBinding(
                  value = @Queue(value = "user.mysql.insert", durable = "true", arguments = {
                          @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                          @Argument(name = "x-dead-letter-exchange", value = "user.dead"),
                          @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                          @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                  }),
                  exchange = @Exchange(value = "mysql", durable = "true"),
                  key = "user.insert"
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
                  key = "url.insert"
          )
    )
    public void listenerUrl(String message) {
        log.info("mysql: {}", message);
        String[] messages = message.split("@");
        urlMapper.insert(Integer.parseInt(messages[1]), messages[0], LocalDateTime.now(), messages[2], messages[3]);
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
                 key = "user.delete"
         )
    )
    public void listenerUserDelete(String message) {
        log.info("mysql: {}", message);
        userMapper.delete(Integer.parseInt(message));
    }

    @RabbitListener(bindings =
         @QueueBinding(
                 value = @Queue(value = "url.mysql.delete", durable = "true", arguments = {
                         @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                         @Argument(name = "x-dead-letter-exchange", value = "url.dead"),
                         @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                         @Argument(name = "x-max-length", value = "1000", type = "java.lang.Integer")
                 }),
                 exchange = @Exchange(value = "mysql", durable = "true"),
                 key = "url.delete"
         )
    )
    public void listenerUrlDelete(String message) {
        log.info("mysql: {}", message);
        urlMapper.delete(Integer.parseInt(message));
    }

    @RabbitListener(bindings =
         @QueueBinding(
                 value = @Queue(value = "comment.mysql.delete", durable = "true", arguments = {
                         @Argument(name = "x-message-ttl", value = "30000", type = "java.lang.Long"),
                         @Argument(name = "x-dead-letter-exchange", value = "comment.dead"),
                         @Argument(name = "x-dead-letter-routing-key", value = "dead"),
                 }),
                 exchange = @Exchange(value = "mysql", durable = "true"),
                 key = "comment.delete"
         )
    )
    public void listenerCommentDelete(JSONArray message) {
        log.info("mysql: {}", message);
        List<Comment> list = JSON.parseArray(message.toString(), Comment.class);
        commentMapper.deleteList(list);
    }






}
