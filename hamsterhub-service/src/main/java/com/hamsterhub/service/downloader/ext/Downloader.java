package com.hamsterhub.service.downloader.ext;

import com.hamsterhub.common.domain.BusinessException;

import java.util.List;

public interface Downloader {

    boolean isReady();

    void setReady(boolean ready);

    Integer getAvailable();

    Boolean connect() throws BusinessException;

    List<DownloaderTask> getAllTask() throws BusinessException;

    Boolean addTask(String tag, String magnet) throws BusinessException;

    Boolean deleteTask(String tag) throws BusinessException;

    DownloaderTask getTask(String tag) throws BusinessException;
}
