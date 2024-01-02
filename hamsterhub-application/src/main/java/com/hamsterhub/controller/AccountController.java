package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.convert.AccountConvert;
import com.hamsterhub.response.LoginResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.AccountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@RestController
@Api(tags = "用户 数据接口")
public class AccountController {

    private List<String> TYPE = Stream.of("管理员", "普通用户").collect(toList());

    @Autowired
    private AccountService accountService;

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
        accountDTO.setType(0);
        accountDTO = accountService.create(accountDTO);

        String token = JwtUtil.createToken(accountDTO.getUsername());
        LoginResponse data = new LoginResponse(accountVO.getUsername(), token);
        return Response.success().msg("注册成功").data(data);
    }

    @ApiOperation("登录账号")
    @PostMapping(value = "/loginAccount")
    public Response LoginAccount(@RequestParam("username") String username,
                                 @RequestParam("password") String password) {
        // 统一小写
        username = username.toLowerCase();

        AccountDTO accountDTO = accountService.query(username);
        // 密码错误
        if (!accountDTO.getPassword().equals(MD5Util.getMd5(password)))
            throw new BusinessException(CommonErrorCode.E_200016);

        String token = JwtUtil.createToken(accountDTO.getUsername());
        LoginResponse data = new LoginResponse(accountDTO.getUsername(), token);
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
        return Response.success().msg("密码修改成功");
    }

    @ApiOperation("校验Token")
    @GetMapping(value = "/checkToken")
    public Boolean checkToken(@RequestParam("token") String token) {
        return JwtUtil.checkToken(token);
    }

    @ApiOperation("刷新Token")
    @GetMapping(value = "/refreshToken")
    public Response refreshToken(@RequestParam("token") String token) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        String newToken = JwtUtil.createToken(accountDTO.getUsername());
        LoginResponse data = new LoginResponse(accountDTO.getUsername(), newToken);
        return Response.success().data(data);
    }
}
