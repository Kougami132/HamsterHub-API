package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.DownloadTaskListDTO;

import java.util.List;

public interface DownloadTaskListService {
    DownloadTaskListDTO create(DownloadTaskListDTO downloadTaskListDTO) throws BusinessException;

    void update(DownloadTaskListDTO downloadTaskListDTO) throws BusinessException;

    void delete(Long downloadTaskListId) throws BusinessException;

    DownloadTaskListDTO query(Long downloadTaskListId) throws BusinessException;

    DownloadTaskListDTO query(Long userId, String tag) throws BusinessException;

    DownloadTaskListDTO query(String tag) throws BusinessException;

    List<DownloadTaskListDTO> fetchByState(Integer state, Integer originType, Long originId) throws BusinessException;

    List<DownloadTaskListDTO> fetchByUser(Long userId) throws BusinessException;

    List<DownloadTaskListDTO> fetchWait(Integer num) throws BusinessException;

    void delete(String tag) throws BusinessException;
}
