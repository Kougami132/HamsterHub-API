package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.convert.PushConfigConvert;
import com.hamsterhub.database.dto.PushConfigDTO;
import com.hamsterhub.database.service.PushConfigService;
import com.hamsterhub.enums.PushType;
import com.hamsterhub.response.PushConfigResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.service.PushService;
import com.hamsterhub.util.ApplicationContextHelper;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.PushConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.events.Comment;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@Tag(name = "推送配置 数据接口")
@RequestMapping("api")
public class PushConfigController {

    private List<String> TYPE = PushType.getTypeList();

    @Autowired
    PushConfigService pushConfigService;

    @Operation(summary = "推送类型")
    @GetMapping(value = "/pushType")
    public Response pushType() {
        return Response.success().data(TYPE);
    }

    @Operation(summary ="查询用户推送配置")
    @GetMapping(value = "/queryPushConfig")
    @Token
    public Response queryPushConfig() {
        Long userId = SecurityUtil.getUser().getId();
        PushConfigDTO pushConfigDTO = pushConfigService.query(userId);
        PushConfigResponse res = PushConfigConvert.INSTANCE.dto2res(pushConfigDTO);
        return Response.success().data(res);
    }

    @Operation(summary ="设置推送配置")
    @PostMapping(value = "/setPushConfig")
    @Token
    public Response setPushConfig(@RequestBody PushConfigVO pushConfigVO) {
        Long userId = SecurityUtil.getUser().getId();

        // type校验
        CommonErrorCode.checkAndThrow(!TYPE.contains(pushConfigVO.getType()), CommonErrorCode.E_130001);
        // param格式校验
        CommonErrorCode.checkAndThrow(!MatchUtil.isJson(pushConfigVO.getParam().toString()), CommonErrorCode.E_130002);

        PushConfigDTO pushConfigDTO = PushConfigConvert.INSTANCE.vo2dto(pushConfigVO);
        pushConfigDTO.setUserId(userId);
        if (pushConfigService.isExist(userId))
            pushConfigService.update(pushConfigDTO);
        else
            pushConfigService.create(pushConfigDTO);
        return Response.success().msg("设置成功");
    }

    @Operation(summary ="测试推送")
    @GetMapping(value = "/testPush")
    @Token
    public Response testPush() {
        Long userId = SecurityUtil.getUser().getId();
        PushConfigDTO pushConfigDTO = pushConfigService.query(userId);

        String beanName = PushType.getBeanNameByType(pushConfigDTO.getType());
        PushService pushService = (PushService) ApplicationContextHelper.getBean(beanName);
        pushService.push(pushConfigDTO.getParam(), "测试");

        return Response.success().msg("消息已推送");
    }
}
