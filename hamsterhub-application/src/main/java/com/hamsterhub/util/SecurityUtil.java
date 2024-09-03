package com.hamsterhub.util;

import com.hamsterhub.database.service.UserService;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.common.util.JwtUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class SecurityUtil {
    public static UserDTO getUser() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        if (servletRequestAttributes != null) {
            HttpServletRequest request = servletRequestAttributes.getRequest();

            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String username = JwtUtil.getUsername(token);
            UserService userService = ApplicationContextHelper.getBean(UserService.class);
            return userService.query(username);
        }
        return null;
    }
}
