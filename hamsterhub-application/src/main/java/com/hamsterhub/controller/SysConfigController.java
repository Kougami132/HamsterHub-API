package com.hamsterhub.controller;


import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.config.SystemConfig;
import com.hamsterhub.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "系统设置 数据接口")
@RequestMapping("api")
public class SysConfigController {
    @Autowired
    private SystemConfig systemConfig;

    private Boolean cache = false;
    private String hash = null;

    @Operation(summary ="设置系统变量(admin)")
    @PostMapping(value = "/setSysConfig")
    @Token("0")
    public Response setSysConfig(@RequestParam("key") String key, @RequestParam("value") String value) {

        if (StringUtil.isBlank(key)) {
            throw new BusinessException(CommonErrorCode.E_800001);
        }

        systemConfig.set(key, value);

        this.cache = false;
        return Response.success().msg("设置成功");
    }

    @Operation(summary ="获取系统变量")
    @GetMapping(value = "/querySysConfig")
    public Response querySysConfig(@RequestParam("hash") String hash) {

        if (!this.cache) {
            this.cache = true;
            this.hash = systemConfig.getCacheId();
        }

        if (!this.hash.equals(hash)) {
            return Response.success().data(systemConfig.getObj());
        }
        else {
            return Response.success();
        }

    }

}
