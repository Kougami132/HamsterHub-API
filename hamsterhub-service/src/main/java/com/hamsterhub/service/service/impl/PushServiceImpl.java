package com.hamsterhub.service.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.service.PushService;
import com.hamsterhub.service.bot.GoCqHttpBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushServiceImpl implements PushService {

    @Autowired
    private GoCqHttpBot goCqHttpBot;

    @Override
    public void pushGoCq(Boolean isGroup, Long targetId, String message) throws BusinessException {
        goCqHttpBot.pushMsg(isGroup, targetId, message);
    }
}
