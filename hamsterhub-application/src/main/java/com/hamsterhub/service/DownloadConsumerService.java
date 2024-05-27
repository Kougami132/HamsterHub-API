package com.hamsterhub.service;

import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.DownloadTask;
import com.hamsterhub.service.entity.MultipartFileDTO;
import com.hamsterhub.service.entity.Torrent;
import com.hamsterhub.service.service.RFileService;
import com.hamsterhub.service.service.StrategyService;
import com.hamsterhub.service.service.VFileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@RocketMQMessageListener(topic = "${rocketmq.topic}", consumerGroup = "${rocketmq.consumer.group}", consumeThreadMax = 3, selectorExpression = "DOWNLOAD")
@Component
public class DownloadConsumerService implements RocketMQListener<DownloadTask> {

    @Autowired
    BitTorrentService bitTorrentService;
    @Autowired
    RedisService redisService;
    @Autowired
    StrategyService strategyService;
    @Autowired
    RFileService rFileService;
    @Autowired
    VFileService vFileService;
    @Autowired
    FileService fileService;

    @SneakyThrows
    @Override
    public void onMessage(DownloadTask downloadTask) {
        String savePath = "temp/" + downloadTask.getTag();
        File dir = new File(savePath);
        if (!dir.exists()) dir.mkdirs();

        if (!bitTorrentService.connect()) {
            log.info("qBittorrent登陆失败");
            return;
        }
        Torrent torrent = bitTorrentService.getTorrent(downloadTask.getTag());
        if (torrent == null)
            if (!bitTorrentService.addTorrent(downloadTask.getTag(), downloadTask.getMagnet(), savePath)) {
                log.error("下载任务添加失败");
                redisService.addTask(downloadTask.getAccountId(), downloadTask.getTag(), "error");
                return;
            }

        log.info("下载任务已开启, tag: {}", downloadTask.getTag());
        while (true) {
            Thread.sleep(1000);

            // 任务是否还存在
            Map<String, String> tasks = redisService.getTasks();
            if (!tasks.containsKey(downloadTask.getTag())) {
                bitTorrentService.deleteTorrent(downloadTask.getTag());
                return;
            }

            torrent = bitTorrentService.getTorrent(downloadTask.getTag());
            if (torrent == null) {
                log.error(" {} 下载出错, 未获取到Torrent", downloadTask.getTag());
                redisService.addTask(downloadTask.getAccountId(), downloadTask.getTag(), "error");
                return;
            }

            // 容量是否足够
            if (dir.getUsableSpace() - torrent.getAmount_left() < 1024 * 1024 * 1024) {
                log.error("容量不足, {} 下载中止", torrent.getName());
                redisService.addTask(downloadTask.getAccountId(), downloadTask.getTag(), "error");
                return;
            }

            log.info("{} 下载状态: {}, {}MB / {}MB", torrent.getName(), torrent.getState(),
                    torrent.getCompleted() / 1024 / 1024, torrent.getTotal_size() / 1024 / 1024);

            if (torrent.isCompleted()) {
                log.info("{} 下载完成", torrent.getName());
                break;
            }
        }

        // DFS遍历下载结果
        File[] files = dir.listFiles();
        for (File i: files)
            createFileInfo(i, downloadTask.getStrategyId(), downloadTask.getParentId(), downloadTask.getAccountId());

        redisService.removeTask(downloadTask.getTag());
    }

    private void createFileInfo(File file, Long strategyId, Long parentId, Long accountId) {
        if (file.isDirectory()) {
            VFileDTO dir = VFileDTO.newDir(file.getName(), strategyId, parentId, accountId);
            dir = vFileService.createDir(dir);

            File[] files = file.listFiles();
            for (File i: files)
                createFileInfo(i, strategyId, dir.getId(), accountId);
        }
        else if (file.isFile()) {
            StrategyDTO strategyDTO = strategyService.query(strategyId);
            MultipartFile multipartFile = getMultipartFile(file);
            String hash = MD5Util.getMd5(multipartFile);
            RFileDTO rFileDTO;
            if (rFileService.isExist(hash, strategyId))
                rFileDTO = rFileService.query(hash, strategyId);
            else
                rFileDTO = fileService.upload(multipartFile, strategyDTO);
            VFileDTO f = VFileDTO.newFile(file.getName(), strategyId, parentId, rFileDTO, accountId);
            vFileService.create(f);
        }
    }

    @SneakyThrows
    private static MultipartFile getMultipartFile(File file) {
        File excelFile = new File(file.getPath());
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        MultipartFile multipartFile = new MultipartFileDTO(file.getPath(), file.getPath(), ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
        return multipartFile;
    }
}