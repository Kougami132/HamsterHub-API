package com.hamsterhub.service.schedule;

import com.hamsterhub.service.mapper.FileLinkMapper;
import com.hamsterhub.service.service.FileLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DataCleanupService {

    @Autowired
    private FileLinkService fileLinkService;

    @Transactional
    @Scheduled(cron = "0 0 4 * * ?") // 每天早上4点执行,清理过期临时直链
    public void deleteByExpiry() {
//        System.out.println("定时任务");
        log.info("定时任务");
        fileLinkService.deleteByExpiry();
    }
}
