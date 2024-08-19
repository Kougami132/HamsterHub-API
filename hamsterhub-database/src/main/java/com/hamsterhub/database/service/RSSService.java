package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.RSSListDTO;
import com.hamsterhub.database.dto.RSSTaskDTO;

import java.util.List;

public interface RSSService {
    RSSListDTO createRSSList(RSSListDTO rssList) throws BusinessException;

    void updateRSSList(RSSListDTO rssList) throws BusinessException;

    void updateRSSListForUser(RSSListDTO rssList) throws BusinessException;

    void deleteRSSList(Long rssListId, Long userId) throws BusinessException;

    RSSListDTO queryRSSList(Long rssListId) throws BusinessException;

    List <RSSListDTO> queryRSSListByUser(Long userId) throws BusinessException;

    List<RSSListDTO> fetchAllRSSList() throws BusinessException;

    void setEnable(Long id, Boolean enable, Long userId) throws BusinessException;

    // ----
    RSSTaskDTO createRSSTask(RSSTaskDTO rssTask) throws BusinessException;

    void updateRSSTask(RSSTaskDTO rssTask) throws BusinessException;

    void deleteRSSTask(Long rssTaskId) throws BusinessException;

    RSSTaskDTO queryRSSTask(Long rssTaskId) throws BusinessException;

    void deleteRSSTaskForUser(Long rssTaskId, Long userId) throws BusinessException;

    List<RSSTaskDTO> queryRSSTasks(Long rssTaskId, Long userId) throws BusinessException;

    void createRSSTasks(List<RSSTaskDTO> rssLists) throws BusinessException;

    void setTaskFinish(Long rssTaskId) throws BusinessException;
}
