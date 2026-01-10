package com.ccc.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.ccc.common.constant.LockConstant;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.threadLocal.BaseConstant;
import com.ccc.common.utils.AliOssUtils;
import com.ccc.service.mapper.CommentMapper;
import com.ccc.service.mapper.UrlMapper;
import com.ccc.service.service.UrlService;
import com.example.pojo.dto.UrlPageDTO;
import com.example.pojo.dto.UrlUpdateDTO;
import com.example.pojo.entity.Comment;
import com.example.pojo.entity.Url;
import com.example.pojo.result.Result;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class UrlServiceImpl implements UrlService {

    @Autowired
    private AliOssUtils ossUtils;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UrlMapper urlMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Result upload(String introduce, String name, MultipartFile file) {
        if(name.isEmpty() || file.isEmpty() || introduce.isEmpty()) {
            return Result.error("参数错误");
        }

        if(file.getSize() >= 1024 * 1024 * 10) {
            return Result.error("文件过大");
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

            UrlServiceImpl urlService = (UrlServiceImpl) AopContext.currentProxy();
            return urlService.tryUpload(file, introduce, name);
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
    public Result tryUpload(MultipartFile file, String introduce, String name) {
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
            String msgR = upload + "@" + currentUser;
            String megM = msgR + "@" + introduce + "@" + name;

            Url url = urlMapper.getByUrl(upload);
            if(url != null) {
                return Result.error("地址已存在");
            }

            rabbitTemplate.convertAndSend("redis", "url.insert", msgR);
            rabbitTemplate.convertAndSend("mysql", "url.insert", megM);
            return Result.success(upload);
        } catch (IOException e) {
            log.error("上传文件异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }



     @Override
     public Result getUrl(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("id不能为空");
        }

        String key = LockConstant.USER_LOCK_GETURL + BaseConstant.getCurrentUser();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        log.info("获取url：{}", id);
        try {
            String json = stringRedisTemplate.opsForValue().get(RedisConstant.USER_GETURL + id);
            if(json != null) {
                if ("".equals(json)) {
                    stringRedisTemplate.expire(RedisConstant.USER_GETURL + id, 10, TimeUnit.MINUTES);
                    return Result.error("url不存在");
                }
                Url url = JSON.parseObject(json, Url.class);
                return Result.success(url);
            }
        } catch (Exception e) {
            return Result.error("查询url异常");
        }

        try {
            b = lock.tryLock(1, 30, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }
            return tryGetUrl(id);
        } catch (InterruptedException e) {
            log.error("获取锁异常：{}", e.getMessage());
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Result.error("获取url异常");
     }

    public Result tryGetUrl(Integer id) {
        String json = stringRedisTemplate.opsForValue().get(RedisConstant.USER_GETURL + id);
        if(json != null) {
            if ("".equals(json)) {
                stringRedisTemplate.expire(RedisConstant.USER_GETURL + id, 10, TimeUnit.MINUTES);
                return Result.error("url不存在");
            }
            Url url = JSON.parseObject(json, Url.class);
            return Result.success(url);
        }

        Url url = urlMapper.getById(id);
        if(url == null) {
            stringRedisTemplate.opsForValue().set(RedisConstant.USER_GETURL + id, "", 10, TimeUnit.MINUTES);
            return Result.error("url不存在");
        }

        stringRedisTemplate.opsForValue().set(RedisConstant.USER_GETURL + id, JSON.toJSONString(url), 10, TimeUnit.MINUTES);
        return Result.success(url);
    }

    @Override
    public Result update(UrlUpdateDTO urlUpdateDTO) {
        if(urlUpdateDTO.getUrl() == null || urlUpdateDTO.getName() == null) {
            return Result.error("参数错误");
        }

        if(urlUpdateDTO.getUrl().isEmpty() || urlUpdateDTO.getName().isEmpty()) {
            return Result.error("url或名字不能为空");
        }

        String key = LockConstant.USER_LOCK_UPDATE + BaseConstant.getCurrentUser();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        log.info("更新url：{}", urlUpdateDTO);

        try {
            b = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            UrlServiceImpl urlService = (UrlServiceImpl) AopContext.currentProxy();
            return urlService.tryUpdate(urlUpdateDTO);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return Result.error("获取锁异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryUpdate(UrlUpdateDTO urlUpdateDTO) {
        Url url = urlMapper.getByUrl(urlUpdateDTO.getUrl());
        if(url == null) {
            stringRedisTemplate.opsForSet().remove(RedisConstant.USER_UPLOAD + BaseConstant.getCurrentUser(), urlUpdateDTO.getUrl());
            log.warn("url不存在：{}", urlUpdateDTO.getUrl());
            return Result.error("url不存在");
        }

        BeanUtil.copyProperties(urlUpdateDTO, url);

        try {
            int row = urlMapper.update(url);
            if (row == 0) {
                log.warn("更新 url 失败：{}", urlUpdateDTO.getUrl());
                Url byId = urlMapper.getById(url.getId());
                throw new RuntimeException("更新失败, 最新版本号为:" + byId.getVersion());
            }
            Set<String> members = stringRedisTemplate.opsForSet().members(RedisConstant.USER_UPLOAD + BaseConstant.getCurrentUser());
            if(members == null || members.isEmpty()) {
                return Result.error("用户没有上传过文件");
            }

            log.info("数据同步");
            Long result = stringRedisTemplate.opsForSet().remove(RedisConstant.USER_UPLOAD + BaseConstant.getCurrentUser(), urlUpdateDTO.getUrl());
            if (result == null || result == 0) {
                log.warn("删除 url 失败：{}", urlUpdateDTO.getUrl());
                return Result.error("url不存在");
            }
            stringRedisTemplate.opsForSet().add(RedisConstant.USER_UPLOAD + BaseConstant.getCurrentUser(), urlUpdateDTO.getUrl());
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新 url 异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result delete(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("id不能为空");
        }

        String key = LockConstant.URL_LOCK_DELETE + BaseConstant.getCurrentUser();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        log.info("删除url：{}", id);

        try {
            b = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            UrlServiceImpl urlService = (UrlServiceImpl) AopContext.currentProxy();
            return urlService.tryDelete(id);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return Result.error("获取锁异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Result tryDelete(Integer id) {
        Url byId = urlMapper.getById(id);
        if(byId == null) {
            log.warn("url不存在：{}", id);
            return Result.error("url不存在");
        }

        if(byId.getUserId() != BaseConstant.getCurrentUser()) {
            log.warn("用户没有权限删除该url：{}", id);
        }

        String url = byId.getUrl();

        try {
            List<Comment> comments = commentMapper.getByUrlId(id);
            if(comments != null && !comments.isEmpty()) {
                rabbitTemplate.convertAndSend("redis", "comment.delete", JSON.toJSON(comments));
                rabbitTemplate.convertAndSend("mysql", "comment.delete", JSON.toJSON(comments));
            }
            rabbitTemplate.convertAndSend("redis", "url.delete", id + "@" + url);
            rabbitTemplate.convertAndSend("mysql", "url.delete", id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除 url 异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result pageSelect(UrlPageDTO urlPageDTO) {
        if(urlPageDTO.getPage() == null || urlPageDTO.getPage() <= 0) {
            return Result.error("页码不能小于1");
        }

        RLock lock = redisson.getLock(LockConstant.URL_LOCK_PAGE + BaseConstant.getCurrentUser());
        boolean b = false;
        log.info("分页查询url：{}", urlPageDTO);

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("获取锁失败");
            }

            return tryPageSelect(urlPageDTO);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return Result.error("获取锁异常");
        }
    }

    public Result tryPageSelect(UrlPageDTO urlPageDTO) {
        PageHelper.startPage(urlPageDTO.getPage(), urlPageDTO.getPageNum());
        Page<Url> url = urlMapper.getByPage(urlPageDTO.getIntroduce());
        List<Url> result = url.getResult();
        return Result.success(result);
    }

    @Override
    public ResponseEntity<byte[]> download(Integer id) {
        if(id == null || id <= 0) {
            return ResponseEntity.badRequest().body("id不能为空".getBytes(StandardCharsets.UTF_8));
        }

        String key = LockConstant.URL_LOCK_DOWNLOAD + BaseConstant.getCurrentUser();
        RLock lock = redisson.getLock(key);
        boolean b = false;

        log.info("下载url：{}", id);

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return ResponseEntity.status(HttpStatus.LOCKED).body("获取锁失败".getBytes(StandardCharsets.UTF_8));
            }
            Result result = tryDownload(id);
            if(result.getCode() != 1) {
                return ResponseEntity.status(500).body(null);
            }

            byte[] fileBytes = (byte[]) result.getData();

            String fileName = "downloaded-file.pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return ResponseEntity.status(500).body("获取锁异常".getBytes(StandardCharsets.UTF_8));
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    private Result tryDownload(Integer id) {
        Url byId = urlMapper.getById(id);
        if(byId == null) {
            log.warn("url不存在：{}", id);
            return Result.error("url不存在");
        }

        String url = byId.getUrl();
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        byte[] bytes = ossUtils.download(fileName);
        if(bytes.length == 0) {
            return Result.error("下载失败");
        }

        return Result.success(bytes);
    }

}
