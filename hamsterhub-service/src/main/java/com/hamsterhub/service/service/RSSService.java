package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.RSSListDTO;
import com.hamsterhub.service.dto.RSSTaskDTO;

import java.util.List;

public interface RSSService {
    RSSListDTO createRSSList(RSSListDTO rssList) throws BusinessException;

    void updateRSSList(RSSListDTO rssList) throws BusinessException;

    void deleteRSSList(Long rssListId) throws BusinessException;

    RSSListDTO queryRSSList(Long rssListId) throws BusinessException;

    List<RSSListDTO> fetchAllRSSList() throws BusinessException;

    // ----
    RSSTaskDTO createRSSTask(RSSTaskDTO rssTask) throws BusinessException;

    void updateRSSTask(RSSTaskDTO rssTask) throws BusinessException;

    void deleteRSSTask(Long rssTaskId) throws BusinessException;

    RSSTaskDTO queryRSSTask(Long rssTaskId) throws BusinessException;

    void createRSSTasks(List<RSSTaskDTO> rssLists) throws BusinessException;
}
