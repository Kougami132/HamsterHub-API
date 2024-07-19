package com.hamsterhub.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.service.BitTorrentService;
import com.hamsterhub.service.DownloadService;
import com.hamsterhub.service.FileService;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.Torrent;
import com.hamsterhub.service.service.RFileService;
import com.hamsterhub.service.service.StrategyService;
import com.hamsterhub.service.service.VFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private Executor downloadTaskExecutor;
    @Autowired
    private RedisService redisService;
    @Autowired
    private BitTorrentService bitTorrentService;
    @Autowired
    private VFileService vFileService;
    @Autowired
    private RFileService rFileService;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private FileService fileService;

    @Override
    @Async("downloadTaskExecutor")
    public void sendDownloadMsg(String tag, String magnet, Long strategyId, Long parentId, Long accountId) throws BusinessException {
        try {
            String savePath = "temp/" + tag;
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();
            if (!bitTorrentService.connect()) {
                log.info("qBittorrent登陆失败");
                redisService.addTask(accountId, tag, "error");
                return;
            }
            Torrent torrent = bitTorrentService.getTorrent(tag);
            if (torrent == null)
                if (!bitTorrentService.addTorrent(tag, magnet)) {
                    log.error("下载任务添加失败");
                    redisService.addTask(accountId, tag, "error");
                    return;
                }

            log.info("下载任务已开启, tag: {}", tag);
            while (true) {
                Thread.sleep(1000);

                // 任务是否还存在
                Map<String, String> tasks = redisService.getTasks();
                if (!tasks.containsKey(tag)) {
                    bitTorrentService.deleteTorrent(tag);
                    return;
                }

                torrent = bitTorrentService.getTorrent(tag);
                if (torrent == null) {
                    log.error(" {} 下载出错, 未获取到Torrent", tag);
                    redisService.addTask(accountId, tag, "error");
                    return;
                }

                // 容量是否足够
                if (torrent.getAmount_left() > 0 && dir.getUsableSpace() - torrent.getAmount_left() < 1024 * 1024 * 1024) {
                    log.error("容量不足, {} 下载中止", torrent.getName());
                    bitTorrentService.deleteTorrent(tag);
                    redisService.addTask(accountId, tag, "error");
                    return;
                }

                log.info("{} 下载状态: {}, {}MB / {}MB", torrent.getName(), torrent.getState(),
                        torrent.getCompleted() / 1024 / 1024, torrent.getTotal_size() / 1024 / 1024);
                if (torrent.getState().equals("error")) log.info("QB保存地址：{}", torrent.getSave_path());

                if (torrent.isCompleted()) {
                    log.info("{} 下载完成", torrent.getName());
                    break;
                }
            }

            // DFS遍历下载结果
            File[] files = dir.listFiles();
            for (File i: files)
                createFileInfo(i, strategyId, parentId, accountId);

            redisService.addTask(accountId, tag, "done");

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("{} 下载任务失败", tag);
            redisService.addTask(accountId, tag, "error");
        }
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
            String hash = MD5Util.getMd5(file);
            RFileDTO rFileDTO;
            if (rFileService.isExist(hash, strategyId))
                rFileDTO = rFileService.query(hash, strategyId);
            else
                rFileDTO = fileService.upload(file, strategyDTO);
            VFileDTO f = VFileDTO.newFile(file.getName(), strategyId, parentId, rFileDTO, accountId);
            vFileService.create(f);
        }
    }
}
