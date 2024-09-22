package com.hamsterhub;

import com.hamsterhub.service.service.PushService;
import com.hamsterhub.service.bot.GoCqHttpBot;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestApplication {

    @Autowired
    PushService pushService;
    @Autowired
    GoCqHttpBot goCqHttpBot;

    @SneakyThrows
    @Test
    public void test() {
//        System.out.println(goCqHttpBot.getBotQQ());;
    }

}
