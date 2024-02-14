package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.convert.AccountConvert;
import com.hamsterhub.response.LoginResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.RedisService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.AccountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.abi.datatypes.Bool;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@RestController
@Api(tags = "用户 数据接口")
public class AccountController {

    private List<String> TYPE = Stream.of("管理员", "普通用户").collect(toList());

    @Autowired
    private AccountService accountService;
    @Autowired
    private RedisService redisService;

    @ApiOperation("用户类型")
    @GetMapping(value = "/accountType")
    public Response strategyType() {
        return Response.success().data(TYPE);
    }

    @ApiOperation("注册账号")
    @PostMapping(value = "/registerAccount")
    public Response registerAccount(@RequestBody AccountVO accountVO) {
        // 统一小写
        accountVO.setUsername(accountVO.getUsername().toLowerCase());

        AccountDTO accountDTO = AccountConvert.INSTANCE.vo2dto(accountVO);
        accountDTO.setType(1);
        accountDTO = accountService.create(accountDTO);

        String token = JwtUtil.createToken(accountDTO.getId(), accountDTO.getUsername(), 1);
        redisService.addToken(accountDTO.getId(), token);
        LoginResponse data = new LoginResponse(accountDTO.getId().toString(), accountVO.getUsername(), token);
        return Response.success().msg("注册成功").data(data);
    }

    @ApiOperation("登录账号")
    @PostMapping(value = "/loginAccount")
    public Response LoginAccount(@RequestParam("username") String username,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "lasting", required = false) Boolean lasting) {
        // 统一小写
        username = username.toLowerCase();

        AccountDTO accountDTO = accountService.query(username);
        // 密码错误
        if (!accountDTO.getPassword().equals(MD5Util.getMd5(password)))
            throw new BusinessException(CommonErrorCode.E_200016);

        Integer expiryDay;
        if (Boolean.TRUE.equals(lasting)) expiryDay = 30;
        else expiryDay = 1;
        String token = JwtUtil.createToken(accountDTO.getId(), accountDTO.getUsername(), expiryDay);
        redisService.addToken(accountDTO.getId(), token);

        LoginResponse data = new LoginResponse(accountDTO.getId().toString(), accountDTO.getUsername(), token);
        return Response.success().msg("登录成功").data(data);
    }

    @ApiOperation("修改密码(token)")
    @PostMapping(value = "/changePassword")
    @Token
    public Response ChangePassword(@RequestParam("oldPassword") String oldPassword,
                                   @RequestParam("newPassword") String newPassword) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 密码错误
        if (!accountDTO.getPassword().equals(MD5Util.getMd5(oldPassword)))
            throw new BusinessException(CommonErrorCode.E_200016);

        accountDTO.setPassword(MD5Util.getMd5(newPassword));
        accountService.update(accountDTO);

        // 清空token
        redisService.delAllToken(accountDTO.getId());
        String token = JwtUtil.createToken(accountDTO.getId(), accountDTO.getUsername(), 7);
        redisService.addToken(accountDTO.getId(), token);

        return Response.success().msg("密码修改成功").data(token);
    }

    @ApiOperation("注销(token)")
    @PostMapping(value = "/logout")
    @Token
    public Response logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        AccountDTO accountDTO = SecurityUtil.getAccount();
        redisService.delToken(accountDTO.getId(), token);
        return Response.success().msg("注销成功");
    }

    @ApiOperation("校验Token")
    @GetMapping(value = "/checkToken")
    @Token
    public Response checkToken() {
        return Response.success();
    }

    @ApiOperation("刷新Token")
    @GetMapping(value = "/refreshToken")
    @Token
    public Response refreshToken(HttpServletRequest request) {
        String oldToken = request.getHeader("Authorization").replace("Bearer ", "");
        AccountDTO accountDTO = SecurityUtil.getAccount();
        String newToken = JwtUtil.createToken(accountDTO.getId(), accountDTO.getUsername(), 7);
        redisService.delToken(accountDTO.getId(), oldToken);
        redisService.addToken(accountDTO.getId(), newToken);
        LoginResponse data = new LoginResponse(accountDTO.getId().toString(), accountDTO.getUsername(), newToken);
        return Response.success().data(data);
    }
}
