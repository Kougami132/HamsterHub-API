package com.hamsterhub.service;

import com.hamsterhub.service.entity.DownloadTask;
import com.hamsterhub.service.entity.Torrent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@RocketMQMessageListener(topic = "${rocketmq.topic}", consumerGroup = "${rocketmq.consumer.group}", consumeThreadMax = 3, selectorExpression = "DOWNLOAD")
@Component
public class DownloadConsumerService implements RocketMQListener<DownloadTask> {

    @Autowired
    BitTorrentService bitTorrentService;

    @SneakyThrows
    @Override
    public void onMessage(DownloadTask downloadTask) {
        if (!bitTorrentService.connect()) {
            log.info("qBittorrent登陆失败");
            return;
        }
        if (!bitTorrentService.addTorrent(downloadTask.getTag(), downloadTask.getMagnet(), downloadTask.getSavePath())) {
            log.info("下载任务添加失败");
            return;
        }

        log.info("下载任务已开启, magnet: {}", downloadTask.getTag(), downloadTask.getMagnet());
        while (true) {
            Thread.sleep(10000);

            Torrent torrent = bitTorrentService.getTorrent(downloadTask.getTag());
            if (torrent == null) continue;
            log.info("{} 下载状态: {}, {}MB / {}MB", torrent.getName(), torrent.getState(),
                    torrent.getCompleted() / 1024 / 1024, torrent.getTotal_size() / 1024 / 1024);

            if (torrent.isCompleted()) {
                log.info("{} 下载完成", torrent.getName());
                break;
            }
        }


    }
}