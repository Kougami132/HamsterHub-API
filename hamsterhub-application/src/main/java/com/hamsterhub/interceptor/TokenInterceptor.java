package com.hamsterhub.interceptor;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Token annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(Token.class);
        } else {
            return true;
        }
        // 如果没有该注解，直接放行
        if (annotation == null)
            return true;
        // 验证token
        String token = request.getHeader("token");
        if (JwtUtil.checkToken(token)) {
            return true;
        }
        // 验证不通过，抛出异常，表示用户未登录
        throw new BusinessException(CommonErrorCode.E_NO_AUTHORITY);
    }

}
