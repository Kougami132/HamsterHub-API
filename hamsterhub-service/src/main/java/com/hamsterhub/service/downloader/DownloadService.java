package com.hamsterhub.service.downloader;

import com.hamsterhub.service.downloader.ext.Downloader;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DownloadTaskListDTO;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public interface DownloadService {
    @PostConstruct
    void loadData();

    Downloader getDownloader(int id);

    Map<Integer, Downloader> getAllDownloaders();

    String addDownloadTaskForUser(AccountDTO user, Integer downloaderId, String root, String parent, String url);

    List<DownloadTaskListDTO> getList(AccountDTO user);

    void deleteDownloadTask(Integer downloaderId, String tag, AccountDTO user);

    void renewDownloader();

    void checkDownloaderTask();
}
