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


    @Scheduled(cron = "30 0 * * * ?")
    public void renewConnect() {
        downloadService.renewDownloader();
    }

    @Transactional
    @Scheduled(cron = "0/10 * * * * ?")
    public void taskCheck(){
        log.info("Checking task...");
        downloadService.checkDownloaderTask();
    }

}
