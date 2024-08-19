package com.hamsterhub.util;

import com.hamsterhub.database.service.AccountService;
import com.hamsterhub.database.dto.AccountDTO;
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

            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String username = JwtUtil.getUsername(token);
            AccountService accountService = ApplicationContextHelper.getBean(AccountService.class);
            return accountService.query(username);
        }
        return null;
    }
}
