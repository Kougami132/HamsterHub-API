package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.convert.RSSListConvert;
import com.hamsterhub.service.convert.RSSTaskConvert;
import com.hamsterhub.service.downloader.DownloadState;
import com.hamsterhub.service.dto.RSSListDTO;
import com.hamsterhub.service.dto.RSSTaskDTO;
import com.hamsterhub.service.entity.RSSList;
import com.hamsterhub.service.entity.RSSTask;
import com.hamsterhub.service.mapper.RSSListMapper;
import com.hamsterhub.service.mapper.RSSTaskMapper;
import com.hamsterhub.service.service.RSSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RSSServiceImpl implements RSSService {
    @Autowired
    private RSSListMapper rssListMapper;

    @Autowired
    private RSSTaskMapper rssTaskMapper;

    @Override
    public RSSListDTO createRSSList(RSSListDTO rssList) throws BusinessException {
        RSSList entity = RSSListConvert.INSTANCE.dto2entity(rssList);
        entity.setId(null);
        rssListMapper.insert(entity);
        return RSSListConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void updateRSSList(RSSListDTO rssList) throws BusinessException {
        RSSList entity = RSSListConvert.INSTANCE.dto2entity(rssList);
        rssListMapper.updateById(entity);
    }

    @Override
    public void deleteRSSList(Long rssListId) throws BusinessException {
        rssListMapper.deleteById(rssListId);
    }

    @Override
    public RSSListDTO queryRSSList(Long rssListId) throws BusinessException {
        RSSList rssList = rssListMapper.selectById(rssListId);
        return RSSListConvert.INSTANCE.entity2dto(rssList);
    }

    @Override
    public List <RSSListDTO> fetchAllRSSList() throws BusinessException {
        LambdaQueryWrapper<RSSList> wrapper = new LambdaQueryWrapper<RSSList>().eq(RSSList::getState, 1);
        List<RSSList> rssLists = rssListMapper.selectList(wrapper);
        return RSSListConvert.INSTANCE.entity2dtoBatch(rssLists);
    }

    // ----
    @Override
    public RSSTaskDTO createRSSTask(RSSTaskDTO rssTask) throws BusinessException {
        RSSTask entity = RSSTaskConvert.INSTANCE.dto2entity(rssTask);
        entity.setId(null);
        rssTaskMapper.insert(entity);
        return RSSTaskConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void updateRSSTask(RSSTaskDTO rssTask) throws BusinessException {
        RSSTask entity = RSSTaskConvert.INSTANCE.dto2entity(rssTask);
        rssTaskMapper.updateById(entity);
    }

    @Override
    public void deleteRSSTask(Long rssTaskId) throws BusinessException {
        rssTaskMapper.deleteById(rssTaskId);
    }

    @Override
    public RSSTaskDTO queryRSSTask(Long rssTaskId) throws BusinessException {
        RSSTask rssTask = rssTaskMapper.selectById(rssTaskId);
        return RSSTaskConvert.INSTANCE.entity2dto(rssTask);
    }

    public boolean isExistTask(Long userID, String url) throws BusinessException {
        LambdaQueryWrapper<RSSTask> wrapper = new LambdaQueryWrapper<RSSTask>()
                .eq(RSSTask::getUserId, userID)
                .eq(RSSTask::getUrl, url);
        return rssTaskMapper.exists(wrapper);
    }

    @Override
    public void createRSSTasks(List<RSSTaskDTO> rssLists) throws BusinessException {
        for (RSSTaskDTO rssList : rssLists) {
            // 存在则标记id为null，表示该任务已经存在(因为后面需要这个id，没这个id无法创建任务)
            if (isExistTask(rssList.getUserId(), rssList.getUrl())) {
                rssList.setId(null);
                continue;
            }

            RSSTask entity = RSSTaskConvert.INSTANCE.dto2entity(rssList);
            entity.setId(null);
            rssTaskMapper.insert(entity);
            // 回传id备用
            rssList.setId(entity.getId());
        }
    }

    @Override
    public void setTaskFinish(Long rssTaskId) throws BusinessException {
        RSSTask entity = new RSSTask();
        entity.setId(rssTaskId);
        entity.setState(DownloadState.FINISH.ordinal());
        rssTaskMapper.updateById(entity);
    }


}
