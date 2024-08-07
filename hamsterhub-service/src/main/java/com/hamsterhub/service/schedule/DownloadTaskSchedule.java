package com.hamsterhub.service.schedule;

import com.hamsterhub.service.downloader.DownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DownloadTaskSchedule {

    @Autowired
    private DownloadService downloadService;


    @Scheduled(cron = "30 0/20 * * * ?")
    public void renewConnect() {
        log.info("Renew Connect");
        downloadService.renewDownloader();
    }

    @Transactional
    @Scheduled(cron = "0/2 * * * * ?")
    public void taskCheck(){
//        log.info("Checking task...");
        downloadService.checkDownloaderTask();
    }

}
