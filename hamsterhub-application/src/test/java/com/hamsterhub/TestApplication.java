package com.hamsterhub;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
public class TestApplication {

    @SneakyThrows
    @Test
    public void test() {
//        producerService.sendDownloadMsg("magnet:?xt=urn:btih:7FKMWXJEM4O6SHRW5JXDQQZHJJ56RDSX", "C:\\Users\\Kougami\\Desktop\\");
//        producerService.sendDownloadMsg("magnet:?xt=urn:btih:SRTWNZQR6HXF3ZFK3PSZ52ZG3KORI4ED", "C:\\Users\\Kougami\\Desktop\\");
        Thread.sleep(60000);
    }

}
