package com.hamsterhub.initialize;

import com.hamsterhub.service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabaseInitialize {

    @Autowired
    AccountService accountService;

    @PostConstruct
    private void init() {
        accountService.init();
    }
}
