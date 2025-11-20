package com.ccc.common.interceptor;

import com.ccc.common.threadLocal.BaseConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Integer currentUser = BaseConstant.getCurrentUser();

        if(currentUser != null) {
            return true;
        } else {
            response.setStatus(401);
            return false;
        }
    }
}
