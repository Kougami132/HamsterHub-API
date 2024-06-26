package com.hamsterhub.initialize;

import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.config.SystemConfig;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.service.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabaseInitialize {

    @Autowired
    AccountService accountService;
    @Autowired
    SysConfigService sysConfigService;

    @PostConstruct
    private void init() {
        accountService.init();
        sysConfigService.init();
    }
}
