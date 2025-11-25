package com.ccc.service.service.impl;

import com.ccc.common.constant.LockConstant;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.threadLocal.BaseConstant;
import com.ccc.service.mapper.CommonMapper;
import com.ccc.service.mapper.UrlMapper;
import com.ccc.service.mapper.UserMapper;
import com.ccc.service.service.CommonService;
import com.example.pojo.entity.Url;
import com.example.pojo.entity.User;
import com.example.pojo.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.integration.IntegrationGraphEndpoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UrlMapper urlMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Result like(Integer urlId) {
        Integer userId = BaseConstant.getCurrentUser();
        Set<String> members = stringRedisTemplate.opsForSet().members(RedisConstant.COMMON_LIKE + urlId);
        String key = LockConstant.COMMON_LOCK_LIKE + userId;
        RLock lock = redissonClient.getLock(key);
        boolean b = false;

        log.info("点赞或取消点赞：{}", urlId);
        try {
            b = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("请勿重复点赞");
            }

            if (members == null || !members.contains(String.valueOf(userId))) {
                return tryLike(urlId, userId);
            } else {
                return tryUnlike(urlId, userId);
            }
        } catch (InterruptedException e) {
            log.warn("获取锁异常：{}", e.getMessage());
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return Result.error("点赞失败");
    }

    public Result tryUnlike(Integer urlId, Integer userId) {
        log.info("取消点赞：{}", urlId);
        Url byId = urlMapper.getById(urlId);
        if(byId == null) {
            log.warn("url不存在：{}", urlId);
            return Result.error("url不存在");
        }
        byId.setLikeCount(byId.getLikeCount() - 1);

        int row = urlMapper.update(byId);
        if(row == 0) {
            log.warn("更新 url 失败：{}", urlId);
            throw new RuntimeException("更新失败");
        }

        stringRedisTemplate.opsForSet().remove(RedisConstant.COMMON_LIKE + urlId, String.valueOf(userId));
        return Result.success();
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryLike(Integer urlId, Integer userId) {
        log.info("点赞：{}", urlId);
        Url byId = urlMapper.getById(urlId);
        if(byId == null) {
            log.warn("url不存在：{}", urlId);
            return Result.error("url不存在");
        }
        byId.setLikeCount(byId.getLikeCount() + 1);

        int row = urlMapper.update(byId);
        if(row == 0) {
            log.warn("更新 url 失败：{}", urlId);
            throw new RuntimeException("更新失败");
        }

        stringRedisTemplate.opsForSet().add(RedisConstant.COMMON_LIKE + urlId, String.valueOf(userId));
        return Result.success();
    }

    @Override
    public Result subscribe(Integer userId) {
        Integer currentUser = BaseConstant.getCurrentUser();
        if(currentUser == userId) {
            return Result.error("不能关注自己");
        }
        Set<String> members = stringRedisTemplate.opsForSet().members(RedisConstant.COMMON_SUBSCRIBE + userId);
        String key = LockConstant.COMMON_LOCK_SUBSCRIBE + currentUser;
        RLock lock = redissonClient.getLock(key);
        boolean b = false;
        
        log.info("关注或取消关注：{}", userId);
        try { 
            b = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("请勿重复关注");
            }
            if (members == null || !members.contains(String.valueOf(currentUser))) {
                return trySubscribe(userId, currentUser);
            } else {
                return tryUnsubscribe(userId, currentUser);
            }
        } catch (InterruptedException e) {
            log.warn("获取锁异常：{}", e.getMessage());
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return Result.error("关注失败");
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryUnsubscribe(Integer userId, Integer currentUser) {
        log.info("取消关注：{}", userId);
        User byId = userMapper.getById(userId);
        if(byId == null) {
            log.warn("用户不存在：{}", userId);
            return Result.error("用户不存在");
        }
        byId.setSubCount(byId.getSubCount() - 1);

        int row = userMapper.update(byId);
        if(row == 0) {
            log.warn("更新用户失败：{}", userId);
            throw new RuntimeException("更新失败");
        }
        stringRedisTemplate.opsForSet().remove(RedisConstant.COMMON_SUBSCRIBE + userId, String.valueOf(currentUser));
        return Result.success();
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result trySubscribe(Integer userId, Integer currentUser) {
        log.info("关注：{}", userId);
        User byId = userMapper.getById(userId);
        if(byId == null) {
            log.warn("用户不存在：{}", userId);
            return Result.error("用户不存在");
        }
        byId.setSubCount(byId.getSubCount() + 1);

        int row = userMapper.update(byId);
        if(row == 0) {
            log.warn("更新用户失败：{}", userId);
            throw new RuntimeException("更新失败");
        }
        stringRedisTemplate.opsForSet().add(RedisConstant.COMMON_SUBSCRIBE + userId, String.valueOf(currentUser));
        return Result.success();
    }
}
