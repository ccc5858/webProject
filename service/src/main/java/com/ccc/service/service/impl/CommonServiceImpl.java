package com.ccc.service.service.impl;

import cn.hutool.core.lang.UUID;
import com.ccc.common.constant.LockConstant;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.threadLocal.BaseConstant;
import com.ccc.common.utils.AliOssUtils;
import com.ccc.service.mapper.CommonMapper;
import com.ccc.service.service.CommonService;
import com.example.pojo.entity.UserWithUrl;
import com.example.pojo.result.Result;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private AliOssUtils ossUtils;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CommonMapper commonMapper;

    @Override
    public Result upload(MultipartFile file) {
        if(file == null) {
            return Result.error("请选择文件");
        }

        if(file.getSize() > 1024 * 1024 * 10) {
            return Result.error("文件大小不能超过10M");
        }

        String key = LockConstant.USER_LOCK_UPLOAD + BaseConstant.getCurrentUser();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        log.info("上传文件：{}", file);
        try {
            b = lock.tryLock(1, 30, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }
            return tryUpload(file);
        } catch (Exception e) {
            log.error("上传文件异常：{}", e.getMessage());
            return Result.error("上传文件异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryUpload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (StringUtils.isBlank(extension)) {
                extension = "tmp";
                log.warn("文件无后缀，默认设为tmp，用户={}, 文件名={}", BaseConstant.getCurrentUser(), originalFilename);
            }
            String fileName = UUID.randomUUID(true).toString() + extension;
            String upload = ossUtils.upload(fileName, file.getBytes());
            Integer currentUser = BaseConstant.getCurrentUser();
            String msg = upload + ":" + currentUser;

            UserWithUrl userWithUrl = commonMapper.getByUrl(upload);
            if(userWithUrl != null) {
                return Result.error("地址已存在");
            }

            commonMapper.insert(currentUser, upload, LocalDateTime.now());
            return Result.success(upload);
        } catch (IOException e) {
            log.error("上传文件异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    // TODO
//    @Override
public Result getUrl(Integer id) {
//        if(id == null || id <= 0) {
//            return Result.error("id不能为空");
//        }
//
//        String key = LockConstant.USER_LOCK_GETURL + BaseConstant.getCurrentUser();
//        RLock lock = redisson.getLock(key);
//        boolean b = false;
//
//        log.info("获取url：{}", id);
//        try {
//            b = lock.tryLock(1, 30, TimeUnit.SECONDS);
//            if(!b) {
//                return Result.error("获取锁失败");
//            }
//        } catch (Exception e) {
//            return Result.error("获取锁异常");
//        }
//
        return Result.success();
   }
}
