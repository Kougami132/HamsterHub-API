package com.hamsterhub.controller;


import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.config.SystemConfig;
import com.hamsterhub.convert.SysConfigConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.SysConfigResponse;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Objects;


@RestController
@Api(tags = "系统设置 数据接口")
public class SysConfigController {
    @Autowired
    private SystemConfig systemConfig;

    private Boolean cache = false;
    private String hash = null;

    @ApiOperation("设置系统变量(admin)")
    @PostMapping(value = "/setSysConfig")
    @Token("0")
    public Response setSysConfig(@RequestParam("key") String key,@RequestParam("value") String value) {

        if(StringUtil.isBlank(key)){
            throw new BusinessException(CommonErrorCode.E_800001);
        }

        systemConfig.set(key, value);

        this.cache = false;
        return Response.success().msg("设置成功");
    }

    @ApiOperation("获取系统变量")
    @GetMapping(value = "/querySysConfig")
    public Response querySysConfig(@RequestParam("hash") String hash) {

        if(!this.cache){
            this.cache = true;
            this.hash = systemConfig.getCacheId();
        }

        if (!this.hash.equals(hash)) {
            return Response.success().data(systemConfig.getObj());
        }  {
            return Response.success();
        }

    }

}
