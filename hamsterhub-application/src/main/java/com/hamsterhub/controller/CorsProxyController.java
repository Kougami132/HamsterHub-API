package com.hamsterhub.controller;


import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.config.SystemConfig;
import com.hamsterhub.service.service.CorsProxyService;
import com.hamsterhub.service.service.impl.CorsProxyServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;



@RestController
@RequestMapping(value = "/api/proxy")
@Tag(name = "用于中转部分依赖外部的api")
public class CorsProxyController {

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private CorsProxyService corsProxyService;


    @Operation(summary ="获取视频列表，多用于获取cid")
    @GetMapping(value = "/queryPageListBili")
    public String queryPageListBili(@RequestParam(required = false) String aid,
                      @RequestParam(required = false)  String bvid) {
        // 验证是否开启
        if(!systemConfig.check(ConfigKey.PROXY_DANMA_BILI)){
            throw new BusinessException(CommonErrorCode.E_100009);
        }

        if(StringUtil.isBlank(aid) && StringUtil.isBlank(bvid)) {
            throw new BusinessException(CommonErrorCode.E_100002);
        }
        return corsProxyService.queryPageListBili(aid,bvid);
    }

    @Operation(summary ="获取弹幕")
    @GetMapping(value = "/queryXmlForBili")
    public String queryXmlForBili(@RequestParam() String cid) {
        // 验证是否开启
        if(!systemConfig.check(ConfigKey.PROXY_DANMA_BILI)){
            throw new BusinessException(CommonErrorCode.E_100009);
        }

        if(StringUtil.isBlank(cid)) {
            throw new BusinessException(CommonErrorCode.E_100002);
        }
        return corsProxyService.queryXmlForBili(cid);
    }

    @Operation(summary ="获取阿里云盘登录二维码")
    @GetMapping(value = "/queryAliQrCode")
    public String queryAliQrCode() {
        return corsProxyService.queryAliQrCode();
    }

    @Operation(summary ="获取阿里云盘登录二维码状态")
    @GetMapping(value = "/queryAliQrCodeStatus")
    public String queryAliQrCodeStatus(@RequestParam() String ck,
                                       @RequestParam() String t) {
        return corsProxyService.queryAliQrCodeStatus(ck, t);
    }



}
