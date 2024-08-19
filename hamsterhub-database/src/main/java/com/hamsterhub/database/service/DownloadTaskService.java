package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.DownloadTaskDTO;

import java.util.List;

public interface DownloadTaskService {
    DownloadTaskDTO create(DownloadTaskDTO downloadTaskListDTO) throws BusinessException;

    void update(DownloadTaskDTO downloadTaskListDTO) throws BusinessException;

    void delete(Long downloadTaskListId) throws BusinessException;

    DownloadTaskDTO query(Long downloadTaskListId) throws BusinessException;

    DownloadTaskDTO query(Long userId, String tag) throws BusinessException;

    DownloadTaskDTO query(String tag) throws BusinessException;

    DownloadTaskDTO queryByIndex(String index) throws BusinessException;

    List<DownloadTaskDTO> fetchByState(Integer state, Integer originType, Long originId) throws BusinessException;

    List<DownloadTaskDTO> fetchByUser(Long userId) throws BusinessException;

    List<DownloadTaskDTO> fetchWait(Integer num, Integer DownloaderId) throws BusinessException;

    void delete(String tag) throws BusinessException;
}
