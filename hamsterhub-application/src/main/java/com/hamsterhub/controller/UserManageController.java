package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.config.SystemConfig;
import com.hamsterhub.convert.UserConvert;
import com.hamsterhub.response.LoginResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.UserResponse;
import com.hamsterhub.service.RedisService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@RestController
@Tag(name = "用户管理 数据接口")
public class UserManageController {

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private AccountService accountService;


    @Operation(summary ="新建账号")
    @PostMapping(value = "/addAccount")
    @Token("0")
    public Response add(@RequestBody UserVO userVO) {
        AccountDTO accountDTO = UserConvert.INSTANCE.vo2dto(userVO);
        accountDTO.setUsername(accountDTO.getUsername().toLowerCase()); // 统一小写
        accountDTO.setPassModified();
        accountDTO.setPassword("AAA123456"); // 默认密码
        accountService.create(accountDTO);
        return Response.success().msg("新建成功");
    }

    @Operation(summary ="删除账号")
    @PostMapping(value = "/delAccount")
    @Token("0")
    public Response del(@RequestParam("id") Long id) {
        accountService.delete(id);
        return Response.success().msg("删除账号");
    }

    @Operation(summary ="更新账号")
    @PostMapping(value = "/updateAccount")
    @Token("0")
    public Response update(@RequestBody UserVO userVO) {
        userVO.setUsername(userVO.getUsername().toLowerCase());

        AccountDTO accountDTO = UserConvert.INSTANCE.vo2dto(userVO);
        accountDTO.setPassModified();
        accountService.updateForAdmin(accountDTO);
        return Response.success().msg("更新成功");
    }

    @Operation(summary ="查询所有账号")
    @GetMapping(value = "/fetchAccountAll")
    @Token("0")
    public Response fetchAll() {
        List<AccountDTO> accountDTOS = accountService.FetchAll();
        List<UserResponse> userResponses = UserConvert.INSTANCE.dto2resBatch(accountDTOS);
        return Response.success().data(userResponses);
    }


}
