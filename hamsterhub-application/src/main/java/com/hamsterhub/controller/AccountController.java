package com.hamsterhub.controller;

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
import com.hamsterhub.vo.ChangePasswordVO;
import com.hamsterhub.vo.AccountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Api(tags = "用户 数据接口")
public class AccountController {

    @Resource
    private AccountService accountService;

    @ApiOperation("注册账号")
    @PostMapping(value = "/registerAccount")
    public Response registerAccount(@RequestBody AccountVO accountVO) {
        AccountDTO accountDTO = AccountConvert.INSTANCE.vo2dto(accountVO);
        accountDTO.setType(0);
        accountService.create(accountDTO);

        String token = JwtUtil.createToken(accountDTO.getUsername());
        LoginResponse data = new LoginResponse(accountVO.getUsername(), token);
        return Response.success().msg("注册成功").data(data);
    }

    @ApiOperation("登录账号")
    @PostMapping(value = "/loginAccount")
    public Response LoginAccount(@RequestBody AccountVO accountVO) {
        AccountDTO accountDTO = accountService.query(accountVO.getUsername());
        // 密码错误
        if (!accountDTO.getPassword().equals(MD5Util.getMd5(accountVO.getPassword())))
            throw new BusinessException(CommonErrorCode.E_200016);

        String token = JwtUtil.createToken(accountDTO.getUsername());
        LoginResponse data = new LoginResponse(accountDTO.getUsername(), token);
        return Response.success().msg("登录成功").data(data);
    }

    @ApiOperation("修改密码(token)")
    @PostMapping(value = "/changePassword")
    public Response ChangePassword(@RequestBody ChangePasswordVO changePasswordVO) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 密码错误
        if (!accountDTO.getPassword().equals(MD5Util.getMd5(changePasswordVO.getOldPassword())))
            throw new BusinessException(CommonErrorCode.E_200016);

        accountDTO.setPassword(MD5Util.getMd5(changePasswordVO.getNewPassword()));
        accountService.update(accountDTO);
        return Response.success().msg("密码修改成功");
    }

    @ApiOperation("校验Token")
    @GetMapping(value = "/checkToken")
    public Boolean checkToken(@RequestParam("token") String token) {
        return JwtUtil.checkToken(token);
    }
}
