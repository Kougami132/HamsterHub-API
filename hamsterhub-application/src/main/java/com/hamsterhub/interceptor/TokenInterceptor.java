package com.hamsterhub.interceptor;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private AccountService accountService;
    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Token annotation;
        if (handler instanceof HandlerMethod)
            annotation = ((HandlerMethod) handler).getMethodAnnotation(Token.class);
        else
            return true;

        // 如果没有该注解，直接放行
        if (annotation == null)
            return true;

        // 验证token
        if (request.getHeaders("Authorization").hasMoreElements()) {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            if (JwtUtil.checkToken(token)) { // 检查JWT
                String username = JwtUtil.getUsername(token);
                AccountDTO accountDTO = accountService.query(username);
                if (!redisService.checkToken(token)) { // 检查token黑名单
                    if (JwtUtil.getCreateTime(token).isAfter(accountDTO.getPassModified())) { // 检查token生成时间
                        // 判断权限
                        String type = accountDTO.getType().toString();
                        String[] groups = annotation.value().split(" ");
                        if (groups[0].equals("") || accountDTO.isAdmin())
                            return true;
                        for (String i: groups)
                            if (i.equals(type))
                                return true;
                    }
                }
            }
        }

        // 验证不通过，抛出异常，表示用户未登录
        throw new BusinessException(CommonErrorCode.E_NO_AUTHORITY);
    }

}
