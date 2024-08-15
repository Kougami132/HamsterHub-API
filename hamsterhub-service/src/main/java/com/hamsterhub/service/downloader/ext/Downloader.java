package com.hamsterhub.service.downloader.ext;

import com.hamsterhub.common.domain.BusinessException;

import java.util.List;

public interface Downloader {

    boolean isReady();

    void setReady(boolean ready);

    Integer getAvailable();

    void setName(String name);

    String getName();

    Integer getType();

    Boolean connect() throws BusinessException;

    List<DownloaderTask> getAllTask() throws BusinessException;

    String addTask(String tag, String magnet) throws BusinessException;

    Boolean deleteTask(String tag) throws BusinessException;

    DownloaderTask getTask(String tag) throws BusinessException;
}
