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
        pushService.pushGoCq(false, 1329623049L, "test");
//        pushService.pushGOCQ("https://bot.kougami.cn/send_msg", true, 182121190L, "test");
//        System.out.println(goCqHttpBot.getBotQQ());;
    }

}
