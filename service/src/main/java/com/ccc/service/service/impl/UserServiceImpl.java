package com.ccc.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.PageResult;
import cn.hutool.db.handler.PageResultHandler;
import cn.hutool.jwt.Claims;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.properties.JwtProperties;
import com.ccc.common.utils.JwtUtils;
import com.ccc.service.mapper.UserMapper;
import com.ccc.service.service.UserService;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.dto.UserSelectPage;
import com.example.pojo.entity.User;
import com.example.pojo.result.Result;
import com.example.pojo.vo.UserVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);
        User user = userMapper.getByName(userLoginDTO.getUsername());
        if(user == null) {
            return Result.error("用户不存在");
        }
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        if(!user.getUsername().equals(username) || !user.getPassword().equals(password)) {
            return Result.error("用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String token = JwtUtils.createToken(claims, jwtProperties.getTtl(), jwtProperties.getSecretKey());
        String msg = user.getId() + ":" + token;
        rabbitTemplate.convertAndSend("redis", "jwt", msg);

        return Result.success(token);
    }

    @Transactional
    @Override
    public Result getUserById(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("非法id");
        }

        log.info("查询用户：{}", id);
        String keyLock = RedisConstant.USER_LOCK + id;
        String key = RedisConstant.USER_INFO + id;
        User user = null;
        UserVO userVO = new UserVO();

        try {
            String json = (String) redisTemplate.opsForValue().get(key);
            if(json != null) {
                if("".equals(json)) {
                    return Result.error("用户不存在");
                }
                user = JSON.parseObject(json, User.class);
                redisTemplate.expire(key, 10, TimeUnit.MINUTES);
                BeanUtil.copyProperties(user, userVO);
                return Result.success(userVO);
            }
        } catch (Exception e) {
            log.error("查询用户异常：{}", e.getMessage());
        }

        RLock lock = redisson.getLock(keyLock);
        boolean b = false;
        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            String json = (String) redisTemplate.opsForValue().get(key);
            if(json != null) {
                if("".equals(json)) {
                    return Result.error("用户不存在");
                }
                user = JSON.parseObject(json, User.class);
                redisTemplate.expire(key, 10, TimeUnit.MINUTES);
                BeanUtil.copyProperties(user, userVO);
                return Result.success(userVO);
            }

            user = userMapper.getById(id);
            if(user == null) {
                redisTemplate.opsForValue().set(key, "", 1, TimeUnit.MINUTES);
                return Result.error("用户不存在");
            }

            redisTemplate.opsForValue().set(key, JSON.toJSONString(user), 10, TimeUnit.MINUTES);
            BeanUtil.copyProperties(user, userVO);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("查询用户异常：{}", e.getMessage());
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.success(userVO);
    }


    @Override
    public Result register(UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);
        String username = userRegisterDTO.getUsername();
        User byName = userMapper.getByName(username);

        if(byName != null) {
            return Result.error("用户名已存在");
        }

        if(!userRegisterDTO.getNewPassword().equals(userRegisterDTO.getConfirmPassword())) {
            return Result.error("密码不一致");
        }

        User user = new User();
        BeanUtil.copyProperties(userRegisterDTO, user);
        userMapper.insert(user);

        return Result.success();
    }

    @Override
    public Result getUser(UserSelectPage userSelectPage) {
        log.info("分页查询用户：{}", userSelectPage);
        PageHelper.startPage(userSelectPage.getPageNum(), userSelectPage.getPageSize());

        User user = new User();
        BeanUtil.copyProperties(userSelectPage, user);

        Page<User> page = userMapper.getUser(user);
        List<User> result = page.getResult();

        return Result.success(result);
    }

    @Override
    public Result update(User user) {
        if(user == null || user.getId() == null || user.getId() <= 0) {
            return Result.error("用户不能为空");
        }

        if(user.getVersion() == null) {
            return Result.error("版本不能为空");
        }

        log.info("更新用户：{}", user);
        String key = RedisConstant.USER_LOCK + user.getId();
        String keyUp = RedisConstant.USER_LOCK_UPDATE + user.getId();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            User byId = userMapper.getById(user.getId());
            if(byId == null) {
                return Result.error("用户不存在");
            }

            int numsRow;
            numsRow = userMapper.update(user);

            if(numsRow == 0) {
                return Result.error("版本号不同");
            }

            redisTemplate.delete(RedisConstant.USER_INFO + user.getId());
        } catch (Exception e) {
            log.error("更新用户异常：{}", e.getMessage());
            return Result.error("更新用户异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.success();
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Result delete(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("非法id");
        }

        String json = (String) redisTemplate.opsForValue().get(RedisConstant.USER_INFO + id);
        if(json == null) {
            return Result.error("用户不存在");
        }
        RLock lock = redisson.getLock(RedisConstant.USER_LOCK_DELETE + id);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            User byId = userMapper.getById(id);
            if(byId == null) {
                return Result.error("用户不存在");
            }

            redisTemplate.delete(RedisConstant.USER_INFO + id);
            userMapper.delete(id);

        } catch (Exception e) {
            throw new RuntimeException("数据库删除用户失败，无对应记录");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.success();
    }
}
