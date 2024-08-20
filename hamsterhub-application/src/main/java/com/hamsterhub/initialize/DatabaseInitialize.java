package com.hamsterhub.initialize;

import com.hamsterhub.database.service.AccountService;
import com.hamsterhub.database.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
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
        log.info("数据库初始化完成");
    }
}
