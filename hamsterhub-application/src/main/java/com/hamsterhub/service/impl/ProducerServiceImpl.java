package com.hamsterhub.service.impl;

import com.hamsterhub.service.entity.DownloadTask;
import com.hamsterhub.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ProducerServiceImpl implements ProducerService {

    @Value("${rocketmq.topic}")
    private String topic;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public String sendDownloadMsg(String magnet, String savePath) {
        String tag = UUID.randomUUID().toString();
        rocketMQTemplate.asyncSend(this.topic + ":DOWNLOAD", MessageBuilder.withPayload(new DownloadTask(tag, magnet, savePath)).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送下载消息成功, tag: {}", tag);
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("发送下载消息失败, tag: {}", tag);
            }
        });
        return tag;
    }
}
