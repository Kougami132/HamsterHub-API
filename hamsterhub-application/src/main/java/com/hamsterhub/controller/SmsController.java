package com.hamsterhub.controller;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.response.Response;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@Tag(name = "验证码 数据接口")
public class SmsController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private SmsService smsService;

    @Operation(summary ="发送手机验证码")
    @GetMapping(value = "/sendPhoneCode")
    public Response sendPhoneCode(@RequestParam("phone") Long phone) {
        // 每日上限
        if (redisService.isPhoneLimited(phone))
            throw new BusinessException(CommonErrorCode.E_200017);
        // cd中
        if (redisService.getPhoneCode(phone) != null)
            throw new BusinessException(CommonErrorCode.E_200018);

        // 生成验证码
        Integer code = new Random().nextInt(900000) + 100000;

        redisService.setPhoneCode(phone, code.toString()); // 保存验证码
        redisService.phoneCount(phone); // 记录次数

        smsService.sendTencentCode(phone, code.toString());

        return Response.success().msg("发送成功");
    }

}
