package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.service.config.SystemConfig;
import com.hamsterhub.response.LoginResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.service.RedisService;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.service.UserService;
import com.hamsterhub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@RestController
@Tag(name = "用户 数据接口")
@RequestMapping("api")
public class UserController {

    private List<String> TYPE = Stream.of("管理员", "普通用户").collect(toList());

    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    @Operation(summary ="用户类型")
    @GetMapping(value = "/userType")
    public Response strategyType() {
        return Response.success().data(TYPE);
    }

    @Operation(summary ="注册账号")
    @PostMapping(value = "/register")
    public Response registerUser(@RequestParam("username") String username,
                                    @RequestParam("password") String password,
                                    @RequestParam("phone") Long phone,
                                    @RequestParam("code") String code) {
        // 验证是否允许注册
        if(!ConfigKey.isTrue(systemConfig.get(ConfigKey.CAN_REGISTER))){
            throw new BusinessException(CommonErrorCode.E_200019);
        }


        // 统一小写
        username = username.toLowerCase();

        // 手机验证码校验
        String phoneCode = redisService.getPhoneCode(phone);
        if (phoneCode == null || !phoneCode.equals(code))
            throw new BusinessException(CommonErrorCode.E_200011);

        UserDTO userDTO = new UserDTO(username, password, 1, phone);
        userDTO = userService.create(userDTO);

        String token = JwtUtil.createToken(userDTO.getId(), userDTO.getUsername(), 1);
        LoginResponse data = new LoginResponse(userDTO.getId().toString(), userDTO.getType().toString(), userDTO.getUsername(), token);
        return Response.success().msg("注册成功").data(data);
    }

    @Operation(summary ="登录账号")
    @PostMapping(value = "/login")
    public Response LoginUser(@RequestParam("username") String username,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "lasting", required = false) Boolean lasting) {
        // 统一小写
        username = username.toLowerCase();

        UserDTO userDTO = userService.query(username);
        // 密码错误
        if (!userDTO.getPassword().equals(MD5Util.getMd5(password)))
            throw new BusinessException(CommonErrorCode.E_200016);

        Integer expiryDay;
        if (Boolean.TRUE.equals(lasting)) expiryDay = 30;
        else expiryDay = 1;
        String token = JwtUtil.createToken(userDTO.getId(), userDTO.getUsername(), expiryDay);

        LoginResponse data = new LoginResponse(userDTO.getId().toString(), userDTO.getType().toString(), userDTO.getUsername(), token);
        return Response.success().msg("登录成功").data(data);
    }

    @Operation(summary ="修改密码(token)")
    @PostMapping(value = "/changePassword")
    @Token
    public Response ChangePassword(@RequestParam("oldPassword") String oldPassword,
                                   @RequestParam("newPassword") String newPassword,
                                   HttpServletRequest request) {
        UserDTO userDTO = SecurityUtil.getUser();
        // 密码错误
        if (!userDTO.getPassword().equals(MD5Util.getMd5(oldPassword)))
            throw new BusinessException(CommonErrorCode.E_200016);

        userDTO.setPassword(MD5Util.getMd5(newPassword));
        userDTO.setPassModified(LocalDateTime.now());
        userService.update(userDTO);

//        // 更换token
//        String oldToken = request.getHeader("Authorization").replace("Bearer ", "");
//        long expiryDay = Duration.between(LocalDateTime.now(), JwtUtil.getExpiryTime(oldToken)).toDays() + 1;
//        String token = JwtUtil.createToken(userDTO.getId(), userDTO.getUsername(), (int)expiryDay);
        return Response.success().msg("密码修改成功");
    }

    @Operation(summary ="注销(token)")
    @PostMapping(value = "/logout")
    @Token
    public Response logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        redisService.addTokenBlacklist(token);
        return Response.success().msg("注销成功");
    }

    @Operation(summary ="校验Token")
    @GetMapping(value = "/checkToken")
    @Token
    public Response checkToken() {
        return Response.success();
    }

    @Operation(summary ="刷新Token")
    @GetMapping(value = "/refreshToken")
    @Token
    public Response refreshToken(HttpServletRequest request) {
        String oldToken = request.getHeader("Authorization").replace("Bearer ", "");
        UserDTO userDTO = SecurityUtil.getUser();
        String newToken = JwtUtil.createToken(userDTO.getId(), userDTO.getUsername(), 7);
        redisService.addTokenBlacklist(oldToken);
        LoginResponse data = new LoginResponse(userDTO.getId().toString(), userDTO.getType().toString(), userDTO.getUsername(), newToken);
        return Response.success().data(data);
    }
}
