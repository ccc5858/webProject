package com.ccc.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.ccc.common.constant.LockConstant;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.properties.JwtProperties;
import com.ccc.common.threadLocal.BaseConstant;
import com.ccc.common.utils.AliOssUtils;
import com.ccc.common.utils.JwtUtils;
import com.ccc.service.annotation.logger;
import com.ccc.service.mapper.UserMapper;
import com.ccc.service.service.UserService;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.dto.UserPageDTO;
import com.example.pojo.dto.UserUpdateDTO;
import com.example.pojo.entity.User;
import com.example.pojo.result.Result;
import com.example.pojo.vo.UserVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private AliOssUtils aliOssUtils;

    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        if(userLoginDTO == null) {
            return Result.error("用户信息不能为空");
        }

        if(userLoginDTO.getUsername() == null || "".equals(userLoginDTO.getUsername())) {
            return Result.error("用户名不能为空");
        }

        if(userLoginDTO.getPassword() == null || "".equals(userLoginDTO.getPassword())) {
            return Result.error("密码不能为空");
        }

        log.info("用户登录：{}", userLoginDTO);
        String key = LockConstant.USER_LOCK_LOGIN + userLoginDTO.getUsername();
        RLock lock = redisson.getLock(key);
        boolean b = false;
        String token;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            User user = userMapper.getByName(userLoginDTO.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            String username = userLoginDTO.getUsername();
            String password = userLoginDTO.getPassword();

            if (!user.getUsername().equals(username) || !user.getPassword().equals(password)) {
                return Result.error("用户名或密码错误");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            token = JwtUtils.createToken(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
            String msg = user.getId() + ":" + token;
            redisTemplate.opsForValue().set(RedisConstant.USER_TOKEN + user.getId(), token, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.success(token);
    }

    @Override
    public Result getUserById(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("非法id");
        }

        log.info("查询用户：{}", id);
        String keyLock = LockConstant.USER_LOCK_GETID + id;
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

            return tryGetUserById(id, user, key, userVO);
        } catch (Exception e) {
            log.error("查询用户异常：{}", e.getMessage());
            return Result.error("查询用户异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public Result tryGetUserById(Integer id, User user, String key, UserVO userVO) {

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
        Set<String> members = redisTemplate.opsForSet().members(RedisConstant.USER_UPLOAD + user.getId());
        userVO.setUrlList(members);
        BeanUtil.copyProperties(user, userVO);
        return Result.success(userVO);
    }


    @Override
    public Result register(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO.getUsername() == null || "".equals(userRegisterDTO.getUsername())) {
            return Result.error("用户名不能为空");
        }

        if (userRegisterDTO.getUsername().length() < 6 || userRegisterDTO.getUsername().length() > 20) {
            return Result.error("用户名长度必须在6-20位之间");
        }

        if (userRegisterDTO.getPassword() == null) {
            return Result.error("请输入密码");
        }

        if (userRegisterDTO.getConfirmPassword() == null) {
            return Result.error("请再次确认密码");
        }

        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            return Result.error("密码不一致");
        }

        log.info("用户注册：{}", userRegisterDTO);
        String username = userRegisterDTO.getUsername();
        String key = LockConstant.USER_LOCK_REGISTER + username;

        RLock lock = redisson.getLock(key);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!b) {
                return Result.error("获取锁失败");
            }

            return tryRegister(userRegisterDTO, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryRegister(UserRegisterDTO userRegisterDTO, String username) {
        User user = userMapper.getByName(username);
        if (user != null) {
            return Result.error("用户已存在");
        }

        User newUser = new User();
        BeanUtil.copyProperties(userRegisterDTO, newUser);
        String msg = JSON.toJSONString(newUser);
        rabbitTemplate.convertAndSend("mysql", "user.insert", msg);

        return Result.success();
    }

    @Override
    public Result getUser(UserPageDTO userPageDTO) {
        if(userPageDTO == null) {
            return Result.error("参数错误");
        }

        log.info("分页查询用户：{}", userPageDTO);
        String key = LockConstant.USER_LOCK_GETUSER + BaseConstant.getCurrentUser();
        List<User> result = new ArrayList<>();

        RLock lock = redisson.getLock(key);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!b) {
                return Result.error("获取锁失败");
            }

            return tryGetUser(userPageDTO, result);
        } catch (Exception e) {
            log.error("分页查询用户异常：{}", e.getMessage());
        } finally {
            if (b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.success(result);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryGetUser(UserPageDTO userPageDTO, List<User> result) {
        PageHelper.startPage(userPageDTO.getPageNum(), userPageDTO.getPageSize());

        User user = new User();
        BeanUtil.copyProperties(userPageDTO, user);

        Page<User> page = userMapper.getUser(user);
        result = page.getResult();

        return Result.success(result);
    }

    @Override
    public Result update(UserUpdateDTO userDto) {
        if(userDto == null) {
            return Result.error("用户不能为空");
        }

        if(userDto.getUsername() == null || "".equals(userDto.getUsername())) {
            return Result.error("用户名不能为空");
        }

        if(userDto.getUsername().length() < 6 || userDto.getUsername().length() > 20) {
            return Result.error("用户名长度必须在6-20位之间");
        }

        if(userDto.getPassword() == null) {
            return Result.error("请输入密码");
        }

        if(userDto.getConfirmPassword() == null) {
            return Result.error("请再次确认密码");
        }

        if(!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            return Result.error("密码不一致");
        }

        User user = userMapper.getByName(userDto.getUsername());

        if(user == null) {
            return Result.error("用户不存在");
        }
        BeanUtil.copyProperties(userDto, user);

        log.info("更新用户：{}", user);
        String key = LockConstant.USER_LOCK_UPDATE + user.getId();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            return tryUpdate(userDto, user);
        } catch (Exception e) {
            log.error("更新用户异常：{}", e.getMessage());
            return Result.error("更新用户异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryUpdate(UserUpdateDTO userDto, User user) {
        int numsRow;
        numsRow = userMapper.update(user);

        if(numsRow == 0) {
            User latestUser = userMapper.getById(user.getId());
            String latestVersion = latestUser != null ? String.valueOf(latestUser.getVersion()) : "未知";
            throw new RuntimeException("数据已被其他用户修改，请刷新后重试（最新版本号：" + latestVersion + "）");
        }

        rabbitTemplate.convertAndSend("redis", "user.delete", user);
        rabbitTemplate.convertAndSend("mysql", "user.insert", user);
        return Result.success();
    }


    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Result delete(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("非法id");
        }

        String json = (String) redisTemplate.opsForValue().get(RedisConstant.USER_INFO + id);
        if(json != null) {
            rabbitTemplate.convertAndSend("redis", "user", id);
        }
        RLock lock = redisson.getLock(LockConstant.USER_LOCK_DELETE + id);
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

            rabbitTemplate.convertAndSend("mysql", "user", id);

        } catch (Exception e) {
            throw new RuntimeException("数据库删除用户失败，无对应记录");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.success();
    }

    @logger
    @Override
    public String text() {
        return "text";
    }

    @Override
    public Result updateImg(MultipartFile file) {
        if(file == null) {
            return Result.error("请选择图片");
        }

        log.info("上传图片：{}", file);
        RLock lock = redisson.getLock(LockConstant.USER_LOCK_UPDATE_IMG + BaseConstant.getCurrentUser());
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            return tryUpdateImg(file);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryUpdateImg(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + suffix;

        try {
            String url = aliOssUtils.upload(fileName, file.getBytes());
            userMapper.updateImg(BaseConstant.getCurrentUser(), url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Result.success();
    }

}
