package com.hamsterhub;

import com.hamsterhub.service.BitTorrentService;
import com.hamsterhub.service.impl.ProducerServiceImpl;
import lombok.SneakyThrows;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestApplication {

    @Autowired
    private ProducerServiceImpl producerService;

    @SneakyThrows
    @Test
    public void test() {
        producerService.sendDownloadMsg("magnet:?xt=urn:btih:7FKMWXJEM4O6SHRW5JXDQQZHJJ56RDSX", "C:\\Users\\Kougami\\Desktop\\");
        producerService.sendDownloadMsg("magnet:?xt=urn:btih:SRTWNZQR6HXF3ZFK3PSZ52ZG3KORI4ED", "C:\\Users\\Kougami\\Desktop\\");
        Thread.sleep(60000);
    }

//    @Test
//    public void test2() {
//        BigInteger privateKeyInt = new BigInteger(256, new SecureRandom());
//        BigInteger publicKeyInt = Sign.publicKeyFromPrivate(privateKeyInt);
//        String publicKey = "04" + publicKeyInt.toString(16);
//        System.out.println(privateKeyInt);
//        System.out.println(publicKeyInt.toString(16));
//    }
}
