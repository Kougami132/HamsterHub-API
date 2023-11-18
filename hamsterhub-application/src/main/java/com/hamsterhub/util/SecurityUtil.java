package com.hamsterhub.util;

import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.common.util.JwtUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class SecurityUtil {
    public static AccountDTO getAccount() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();

            String token = request.getHeader("token");
            String username = JwtUtil.getUsername(token);
            AccountService accountService = ApplicationContextHelper.getBean(AccountService.class);
            return accountService.query(username);
        }
        return null;
    }
}
