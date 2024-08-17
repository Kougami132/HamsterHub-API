package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.convert.DownloadTaskListConvert;
import com.hamsterhub.service.dto.DownloadTaskListDTO;
import com.hamsterhub.service.entity.DownloadTaskList;
import com.hamsterhub.service.mapper.DownloadTaskListMapper;
import com.hamsterhub.service.service.DownloadTaskListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DownloadTaskListServiceImpl implements DownloadTaskListService {

    @Autowired
    private DownloadTaskListMapper downloadTaskListMapper;

    @Override
    public DownloadTaskListDTO create(DownloadTaskListDTO downloadTaskListDTO) throws BusinessException {
        DownloadTaskList entity = DownloadTaskListConvert.INSTANCE.dto2entity(downloadTaskListDTO);
        entity.setId(null);
        downloadTaskListMapper.insert(entity);
        return DownloadTaskListConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void update(DownloadTaskListDTO downloadTaskListDTO) throws BusinessException {
        DownloadTaskList entity = DownloadTaskListConvert.INSTANCE.dto2entity(downloadTaskListDTO);
        downloadTaskListMapper.updateById(entity);
    }

    @Override
    public void delete(Long downloadTaskListId) throws BusinessException {
        downloadTaskListMapper.deleteById(downloadTaskListId);
    }

    @Override
    public DownloadTaskListDTO query(Long downloadTaskListId) throws BusinessException {
        DownloadTaskList downloadTaskList = downloadTaskListMapper.selectById(downloadTaskListId);
        return DownloadTaskListConvert.INSTANCE.entity2dto(downloadTaskList);
    }

    @Override
    public DownloadTaskListDTO query(Long userId, String tag) throws BusinessException {
        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getTag,tag).eq(DownloadTaskList::getUserId,userId);

        DownloadTaskList downloadTaskList = downloadTaskListMapper.selectOne(wrapper);
        return DownloadTaskListConvert.INSTANCE.entity2dto(downloadTaskList);
    }

    @Override
    public DownloadTaskListDTO query(String tag) throws BusinessException {
        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getTag,tag);

        DownloadTaskList downloadTaskList = downloadTaskListMapper.selectOne(wrapper);
        return DownloadTaskListConvert.INSTANCE.entity2dto(downloadTaskList);
    }

    @Override
    public DownloadTaskListDTO queryByIndex(String index) throws BusinessException {
        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getTaskIndex,index).last("limit 1");

        DownloadTaskList downloadTaskList = downloadTaskListMapper.selectOne(wrapper);
        return DownloadTaskListConvert.INSTANCE.entity2dto(downloadTaskList);
    }

    @Override
    public List<DownloadTaskListDTO> fetchByState(Integer state,Integer originType, Long originId) throws BusinessException {
        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getState, state)
                .eq(DownloadTaskList::getOriginType, originType)
                .eq(DownloadTaskList::getOriginId,originId);

        List<DownloadTaskList> lists = downloadTaskListMapper.selectList(wrapper);
        return DownloadTaskListConvert.INSTANCE.entity2dtoBatch (lists);
    }

    @Override
    public List<DownloadTaskListDTO> fetchByUser(Long userId) throws BusinessException {
        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getUserId,userId)
                .orderByDesc(DownloadTaskList::getState);

        List<DownloadTaskList> lists = downloadTaskListMapper.selectList(wrapper);
        return DownloadTaskListConvert.INSTANCE.entity2dtoBatch (lists);
    }

    @Override
    public List<DownloadTaskListDTO> fetchWait(Integer num, Integer DownloaderId) throws BusinessException {

        if (num == null || num <= 0) {
            return null;
        }

        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getDownloader,DownloaderId)
                .eq(DownloadTaskList::getState,0) // 仅获取等待状态的任务
                .last("limit "+num);

        List<DownloadTaskList> lists = downloadTaskListMapper.selectList(wrapper);
        return DownloadTaskListConvert.INSTANCE.entity2dtoBatch (lists);
    }

    @Override
    public void delete(String tag) throws BusinessException {
        LambdaQueryWrapper<DownloadTaskList> wrapper = new LambdaQueryWrapper<DownloadTaskList>()
                .eq(DownloadTaskList::getTag,tag);
        downloadTaskListMapper.delete(wrapper);
    }
}
