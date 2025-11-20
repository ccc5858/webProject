package com.ccc.common.interceptor;

import com.ccc.common.constant.RedisConstant;
import com.ccc.common.properties.JwtProperties;
import com.ccc.common.threadLocal.BaseConstant;
import com.ccc.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RefreshInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtproperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(jwtproperties.getTokenName());
        if(token == null) {
            return true;
        }

        try {
            Claims claims = JwtUtils.parseToken(jwtproperties.getSecretKey(), token);

            if (claims == null) {
                return true;
            }

            String s = stringRedisTemplate.opsForValue().get(RedisConstant.USER_TOKEN + claims.getId());
            if (s == null) {
                return true;
            }

            BaseConstant.setCurrentUser((int) claims.get("id"));
            stringRedisTemplate.expire(RedisConstant.USER_TOKEN + claims.getId(), jwtproperties.getTtl(), TimeUnit.MILLISECONDS);

            return true;
        } catch (Exception e) {
            return true;
        }


    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseConstant.removeCurrentUser();
    }
}
