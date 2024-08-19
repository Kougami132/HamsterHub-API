package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.convert.DownloadTaskConvert;
import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.database.entity.DownloadTask;
import com.hamsterhub.database.mapper.DownloadTaskMapper;
import com.hamsterhub.database.service.DownloadTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DownloadTaskServiceImpl implements DownloadTaskService {

    @Autowired
    private DownloadTaskMapper DownloadTaskMapper;

    @Override
    public DownloadTaskDTO create(DownloadTaskDTO DownloadTaskDTO) throws BusinessException {
        DownloadTask entity = DownloadTaskConvert.INSTANCE.dto2entity(DownloadTaskDTO);
        entity.setId(null);
        DownloadTaskMapper.insert(entity);
        return DownloadTaskConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void update(DownloadTaskDTO DownloadTaskDTO) throws BusinessException {
        DownloadTask entity = DownloadTaskConvert.INSTANCE.dto2entity(DownloadTaskDTO);
        DownloadTaskMapper.updateById(entity);
    }

    @Override
    public void delete(Long DownloadTaskId) throws BusinessException {
        DownloadTaskMapper.deleteById(DownloadTaskId);
    }

    @Override
    public DownloadTaskDTO query(Long DownloadTaskId) throws BusinessException {
        DownloadTask DownloadTask = DownloadTaskMapper.selectById(DownloadTaskId);
        return DownloadTaskConvert.INSTANCE.entity2dto(DownloadTask);
    }

    @Override
    public DownloadTaskDTO query(Long userId, String tag) throws BusinessException {
        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getTag,tag).eq(DownloadTask::getUserId,userId);

        DownloadTask DownloadTask = DownloadTaskMapper.selectOne(wrapper);
        return DownloadTaskConvert.INSTANCE.entity2dto(DownloadTask);
    }

    @Override
    public DownloadTaskDTO query(String tag) throws BusinessException {
        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getTag,tag);

        DownloadTask DownloadTask = DownloadTaskMapper.selectOne(wrapper);
        return DownloadTaskConvert.INSTANCE.entity2dto(DownloadTask);
    }

    @Override
    public DownloadTaskDTO queryByIndex(String index) throws BusinessException {
        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getTaskIndex,index).last("limit 1");

        DownloadTask DownloadTask = DownloadTaskMapper.selectOne(wrapper);
        return DownloadTaskConvert.INSTANCE.entity2dto(DownloadTask);
    }

    @Override
    public List<DownloadTaskDTO> fetchByState(Integer state,Integer originType, Long originId) throws BusinessException {
        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getState, state)
                .eq(DownloadTask::getOriginType, originType)
                .eq(DownloadTask::getOriginId,originId);

        List<DownloadTask> lists = DownloadTaskMapper.selectList(wrapper);
        return DownloadTaskConvert.INSTANCE.entity2dtoBatch (lists);
    }

    @Override
    public List<DownloadTaskDTO> fetchByUser(Long userId) throws BusinessException {
        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getUserId,userId)
                .orderByDesc(DownloadTask::getState);

        List<DownloadTask> lists = DownloadTaskMapper.selectList(wrapper);
        return DownloadTaskConvert.INSTANCE.entity2dtoBatch (lists);
    }

    @Override
    public List<DownloadTaskDTO> fetchWait(Integer num, Integer DownloaderId) throws BusinessException {

        if (num == null || num <= 0) {
            return null;
        }

        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getDownloader,DownloaderId)
                .eq(DownloadTask::getState,0) // 仅获取等待状态的任务
                .last("limit "+num);

        List<DownloadTask> lists = DownloadTaskMapper.selectList(wrapper);
        return DownloadTaskConvert.INSTANCE.entity2dtoBatch (lists);
    }

    @Override
    public void delete(String tag) throws BusinessException {
        LambdaQueryWrapper<DownloadTask> wrapper = new LambdaQueryWrapper<DownloadTask>()
                .eq(DownloadTask::getTag,tag);
        DownloadTaskMapper.delete(wrapper);
    }
}
