package com.hamsterhub.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.service.PushService;
import com.hamsterhub.service.bot.GoCqHttpBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("goCqPushService")
@Slf4j
public class GoCqHttpPushServiceImpl implements PushService {

    @Autowired
    private GoCqHttpBot goCqHttpBot;

    @Override
    public void push(String params, String message) throws BusinessException {
        String type = "";
        Long targetId = 0L;
        try {
            JSONObject param = JSON.parseObject(params);
            type = param.getString("type");
            targetId = param.getLong("targetId");
        }
        catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_130002);
        }
        goCqHttpBot.pushMsg(type.equals("group"), targetId, message);
    }
}
