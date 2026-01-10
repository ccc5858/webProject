package com.ccc.service.service.impl;

import com.ccc.common.constant.LockConstant;
import com.ccc.common.constant.RedisConstant;
import com.ccc.common.threadLocal.BaseConstant;
import com.ccc.service.mapper.CommentMapper;
import com.ccc.service.service.CommentService;
import com.ccc.service.service.CommonService;
import com.example.pojo.dto.CommentAddDTO;
import com.example.pojo.dto.CommentPageDTO;
import com.example.pojo.entity.Comment;
import com.example.pojo.result.Result;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Result add(CommentAddDTO commentAddDTO) {
        if(BaseConstant.getCurrentUser() == null) {
            return Result.error("请先登录");
        }

        log.info("添加评论：{}", commentAddDTO);
        Integer id = BaseConstant.getCurrentUser();
        RLock lock = redissonClient.getLock(LockConstant.COMMENT_LOCK_ADD + id);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("请勿频繁操作");
            }

            CommentService commentService = (CommentService) AopContext.currentProxy();
            return commentService.tryAdd(commentAddDTO, id);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return Result.error("添加评论失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result tryAdd(CommentAddDTO commentAddDTO, Integer id) {
        try {
            commentMapper.add(commentAddDTO, id, LocalDateTime.now());
        } catch (Exception e) {
            log.error("添加评论异常：{}", e.getMessage());
            throw new RuntimeException("添加评论异常");
        }
        return Result.success();
    }

    @Override
    public Result getPageComment(CommentPageDTO commentPageDTO) {
        if(BaseConstant.getCurrentUser() == null) {
            return Result.error("请先登录");
        }

        if(commentPageDTO.getPage() == null || commentPageDTO.getPage() <= 0) {
            return Result.error("页码不能小于1");
        }

        if(commentPageDTO.getPageNum() == null || commentPageDTO.getPageNum() <= 0) {
            return Result.error("页数不能小于1");
        }

        log.info("获取评论：{}", commentPageDTO);
        Integer id = BaseConstant.getCurrentUser();
        RLock lock = redissonClient.getLock(LockConstant.COMMENT_LOCK_GET + id);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("请勿频繁操作");
            }

            return tryGetPageComment(commentPageDTO);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return Result.error("获取锁异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public Result tryGetPageComment(CommentPageDTO commentPageDTO) {
        try {
            PageHelper.startPage(commentPageDTO.getPageNum(), commentPageDTO.getPage());
            Page<Comment> page = commentMapper.getPage(commentPageDTO);
            return Result.success(page.getResult());
        } catch (Exception e) {
            log.error("获取评论异常：{}", e.getMessage());
            return Result.error("获取评论异常");
        }
    }

    @Override
    public Result like(Integer id) {
        if(BaseConstant.getCurrentUser() == null) {
            return Result.error("请先登录");
        }

        Comment byId = commentMapper.getById(id);
        if(byId == null) {
            return Result.error("评论不存在");
        }

        log.info("点赞或者取消点赞：{}", id);
        Integer userId = BaseConstant.getCurrentUser();
        String key = RedisConstant.COMMENT_LIKE + byId.getUrlId() + ":" + byId.getParentId() + ":" + id;
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        RLock lock = redissonClient.getLock(LockConstant.COMMENT_LOCK_LIKE + id);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("请勿频繁操作");
            }

            CommentService commentService = (CommentService) AopContext.currentProxy();
            if(members != null && members.contains(userId.toString())) {
                return commentService.tryUnLike(id, userId, key, byId);
            } else {
                return commentService.tryLike(id, userId, key, byId);
            }


        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return Result.error("获取锁异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result tryLike(Integer id, Integer userId, String key, Comment byId) {
        byId.setLikeCount(byId.getLikeCount() + 1);

        try {
            log.info("点赞：{}", id);
            int row = commentMapper.update(byId);
            if(row == 0) {
                throw new RuntimeException("点赞异常");
            }
            stringRedisTemplate.opsForSet().add(key, userId.toString());
        } catch (Exception e) {
            log.error("点赞异常：{}", e.getMessage());
            throw new RuntimeException("点赞异常");
        }

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result tryUnLike(Integer id, Integer userId, String key, Comment byId) {
        byId.setLikeCount(byId.getLikeCount() - 1);

        try {
            log.info("取消点赞：{}", id);
            int row = commentMapper.update(byId);
            if(row == 0) {
                throw new RuntimeException("取消点赞异常");
            }
            stringRedisTemplate.opsForSet().remove(key, userId.toString());
        } catch (Exception e) {
            log.error("取消点赞异常：{}", e.getMessage());
            throw new RuntimeException("取消点赞异常");
        }

        return Result.success();
    }

    @Override
    public Result delete(Integer id) {
        if(id == null || id <= 0) {
            return Result.error("id不能为空");
        }

        log.info("删除评论：{}", id);
        Integer userId = BaseConstant.getCurrentUser();
        String key = LockConstant.COMMENT_LOCK_DELETE + userId;
        RLock lock = redissonClient.getLock(key);
        boolean b = false;

        try {
            b = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if(!b) {
                return Result.error("请勿频繁操作");
            }

            return tryDelete(id);
        } catch (Exception e) {
            log.error("获取锁异常：{}", e.getMessage());
            return Result.error("获取锁异常");
        } finally {
            if(b && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result tryDelete(Integer id) {
        Comment byId = commentMapper.getById(id);
        if (byId == null) {
            log.warn("评论不存在：{}", id);
            return Result.error("评论不存在");
        }

        if (byId.getUserId() != BaseConstant.getCurrentUser() && byId.getCurrUserId() != BaseConstant.getCurrentUser()) {
            log.warn("用户id不匹配：{}", id);
            return Result.error("用户id不匹配");
        }

        try {
            commentMapper.deleteById(id);
            stringRedisTemplate.delete(RedisConstant.COMMENT_LIKE + byId.getUrlId() + ":" + byId.getParentId() + ":" + id);
        } catch (Exception e) {
            log.error("删除评论异常：{}", e.getMessage());
            throw new RuntimeException("删除评论异常");
        }

        return Result.success();
    }
}
