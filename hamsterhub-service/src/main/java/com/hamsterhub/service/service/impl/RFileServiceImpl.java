package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.convert.RFileConvert;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.entity.RFile;
import com.hamsterhub.service.mapper.RFileMapper;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.RFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class RFileServiceImpl implements RFileService {

    @Autowired
    private RFileMapper rFileMapper;
    @Autowired
    private DeviceService deviceService;

    @Override
    public RFileDTO create(RFileDTO rFileDTO) throws BusinessException {
        // 传入对象为空
        if (rFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // hash值为空
        if (StringUtil.isBlank(rFileDTO.getHash()))
            throw new BusinessException(CommonErrorCode.E_500003);
        // 相同hash值文件已存在
        if (rFileMapper.selectCount(new LambdaQueryWrapper<RFile>().eq(RFile::getHash, rFileDTO.getHash())) > 0)
            throw new BusinessException(CommonErrorCode.E_500002);
        // 设备不存在
        if (!deviceService.isExist(rFileDTO.getDeviceId()))
            throw new BusinessException(CommonErrorCode.E_300001);

        RFile entity = RFileConvert.INSTANCE.dto2entity(rFileDTO);
        entity.setId(null);
        rFileMapper.insert(entity);
        return RFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long rFileId) throws BusinessException {
        // 传入对象为空
        if (rFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(rFileId))
            throw new BusinessException(CommonErrorCode.E_500001);

        rFileMapper.deleteById(rFileId);
    }

    @Override
    public void update(RFileDTO rFileDTO) throws BusinessException {
        // 传入对象为空
        if (rFileDTO == null || rFileDTO.getId() == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(rFileDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_500001);
        // 相同hash值文件已存在
        if (rFileMapper.selectCount(new LambdaQueryWrapper<RFile>().eq(RFile::getHash, rFileDTO.getHash())) > 0 && !rFileMapper.selectOne(new LambdaQueryWrapper<RFile>().eq(RFile::getHash, rFileDTO.getHash())).getId().equals(rFileDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_500002);
        // 设备不存在
        if (!deviceService.isExist(rFileDTO.getDeviceId()))
            throw new BusinessException(CommonErrorCode.E_300001);

        RFile entity = RFileConvert.INSTANCE.dto2entity(rFileDTO);
        rFileMapper.updateById(entity);
    }

    @Override
    public RFileDTO query(Long rFileId) throws BusinessException {
        // 传入对象为空
        if (rFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(rFileId))
            throw new BusinessException(CommonErrorCode.E_500001);

        RFile entity = rFileMapper.selectById(rFileId);
        return RFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public Boolean isExist(Long rFileId) throws BusinessException {
        return rFileMapper.selectCount(new LambdaQueryWrapper<RFile>().eq(RFile::getId, rFileId)) > 0;
    }

    @Override
    public Boolean isExist(String hash) throws BusinessException {
        return rFileMapper.selectCount(new LambdaQueryWrapper<RFile>().eq(RFile::getHash, hash)) > 0;
    }
}
