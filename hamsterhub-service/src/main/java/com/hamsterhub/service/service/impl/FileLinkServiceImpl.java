package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.convert.FileLinkConvert;
import com.hamsterhub.service.dto.FileLinkDTO;
import com.hamsterhub.service.entity.FileLink;
import com.hamsterhub.service.mapper.FileLinkMapper;
import com.hamsterhub.service.service.FileLinkService;
import com.hamsterhub.service.service.RFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileLinkServiceImpl implements FileLinkService {

    @Autowired
    private FileLinkMapper fileLinkMapper;
    @Autowired
    private RFileService rFileService;

    @Override
    public FileLinkDTO create(FileLinkDTO fileLinkDTO) throws BusinessException {
        // 实际文件不存在
        if (!rFileService.isExist(fileLinkDTO.getRFileId()))
            throw new BusinessException(CommonErrorCode.E_600001);
        // ticket已存在
        if (isExist(fileLinkDTO.getTicket()))
            throw new BusinessException(CommonErrorCode.E_600019);

        FileLink entity = FileLinkConvert.INSTANCE.dto2entity(fileLinkDTO);
        entity.setId(null);
        fileLinkMapper.insert(entity);
        return FileLinkConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void update(FileLinkDTO fileLinkDTO) throws BusinessException {
        // ticket已存在
        if (isExist(fileLinkDTO.getTicket()) && !fileLinkMapper.selectOne(new LambdaQueryWrapper<FileLink>().eq(FileLink::getTicket, fileLinkDTO.getTicket())).getId().equals(fileLinkDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600019);

        FileLink entity = FileLinkConvert.INSTANCE.dto2entity(fileLinkDTO);
        fileLinkMapper.updateById(entity);
    }

    @Override
    public FileLinkDTO query(Long rFileId) throws BusinessException {
        if (!isExist(rFileId))
            throw new BusinessException(CommonErrorCode.E_600017);

        FileLink entity = fileLinkMapper.selectOne(new LambdaQueryWrapper<FileLink>().eq(FileLink::getRFileId, rFileId));
        return FileLinkConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public FileLinkDTO query(String ticket) throws BusinessException {
        if (!isExist(ticket))
            throw new BusinessException(CommonErrorCode.E_600017);

        FileLink entity = fileLinkMapper.selectOne(new LambdaQueryWrapper<FileLink>().eq(FileLink::getTicket, ticket));
        return FileLinkConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public Boolean isExist(Long rFileId) throws BusinessException {
        return fileLinkMapper.selectCount(new LambdaQueryWrapper<FileLink>().eq(FileLink::getRFileId, rFileId)) > 0;
    }

    @Override
    public Boolean isExist(String ticket) throws BusinessException {
        return fileLinkMapper.selectCount(new LambdaQueryWrapper<FileLink>().eq(FileLink::getTicket, ticket)) > 0;
    }
}
