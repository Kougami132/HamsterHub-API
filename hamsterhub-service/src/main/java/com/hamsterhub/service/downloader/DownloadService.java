package com.hamsterhub.service.downloader;

import com.hamsterhub.service.downloader.ext.Downloader;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.service.entity.DownloaderOption;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public interface DownloadService {
    @PostConstruct
    void loadData();

    Downloader getDownloader(int id);

    Map<Integer, Downloader> getAllDownloaders();

    List<DownloaderOption> getDownloaderOption(UserDTO user);

    String addDownloadTaskForUser(UserDTO user, Integer downloaderId, String root,
                                  String parent, String url, String name);

    List<DownloadTaskDTO> getList(UserDTO user);

    void deleteDownloadTask(Integer downloaderId, String tag, UserDTO user);

    void renewDownloader();

    void checkDownloaderTask();
}
