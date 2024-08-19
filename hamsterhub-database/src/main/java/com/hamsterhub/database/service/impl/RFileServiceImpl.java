package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.convert.RFileConvert;
import com.hamsterhub.database.dto.RFileDTO;
import com.hamsterhub.database.entity.RFile;
import com.hamsterhub.database.mapper.RFileMapper;
import com.hamsterhub.database.service.DeviceService;
import com.hamsterhub.database.service.DeviceStrategyService;
import com.hamsterhub.database.service.RFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RFileServiceImpl implements RFileService {

    @Autowired
    private RFileMapper rFileMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceStrategyService deviceStrategyService;

    @Override
    public RFileDTO create(RFileDTO rFileDTO) throws BusinessException {
        // 传入对象为空
        if (rFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // hash值为空
        if (StringUtil.isBlank(rFileDTO.getHash()))
            throw new BusinessException(CommonErrorCode.E_500003);
        // 相同hash值文件已存在
//        LambdaQueryWrapper<RFile> wrapper = new LambdaQueryWrapper<RFile>().eq(RFile::getHash, rFileDTO.getHash());
//        if (rFileMapper.selectCount(wrapper) > 0) {
//            // 同hash值文件是否在同一策略
//            Long strategyId = deviceStrategyService.queryStrategyId(rFileDTO.getDeviceId());
//            List<RFile> rFiles = rFileMapper.selectList(wrapper);
//            for (RFile rFile: rFiles)
//                if (deviceStrategyService.queryStrategyId(rFile.getDeviceId()).equals(strategyId))
//                    throw new BusinessException(CommonErrorCode.E_500002);
//        }

        // 设备不存在
        if (!deviceService.isExist(rFileDTO.getDeviceId()))
            throw new BusinessException(CommonErrorCode.E_300001);

        RFile entity = RFileConvert.INSTANCE.dto2entity(rFileDTO);
        entity.setId(null);
        rFileMapper.insert(entity);
        return RFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public RFileDTO createTemp(RFileDTO rFileDTO) throws BusinessException {
        // 传入对象为空
        if (rFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // hash值为空
        if (StringUtil.isBlank(rFileDTO.getHash()))
            throw new BusinessException(CommonErrorCode.E_500003);

        RFile entity = RFileConvert.INSTANCE.dto2entity(rFileDTO);
        entity.setDeviceId(-1L);
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
    public void deleteForce(Long rFileId) throws BusinessException {
        // 传入对象为空
        if (rFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);

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
    public RFileDTO query(String hash, Long strategyId) throws BusinessException {
        // 传入对象为空
        if (hash == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(hash, strategyId))
            throw new BusinessException(CommonErrorCode.E_500001);

        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyId);
        List<RFile> rFiles = rFileMapper.selectList(new LambdaQueryWrapper<RFile>().eq(RFile::getHash, hash));
        if (rFiles.size() != 0)
            for (RFile i: rFiles)
                if (deviceIds.contains(i.getDeviceId()))
                    return RFileConvert.INSTANCE.entity2dto(i);
        return null;
    }

    @Override
    public Boolean isExist(Long rFileId) throws BusinessException {
        return rFileMapper.selectCount(new LambdaQueryWrapper<RFile>().eq(RFile::getId, rFileId)) > 0;
    }

    @Override
    public Boolean isExist(String hash, Long strategyId) throws BusinessException {
//        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyId);
        List<RFile> rFiles = rFileMapper.selectList(new LambdaQueryWrapper<RFile>()
                .eq(RFile::getHash, hash)
                .eq(RFile::getDeviceId, strategyId));
//        if (rFiles.size() != 0)
//            for (RFile i: rFiles)
//                if (deviceIds.contains(i.getDeviceId()))
//                    return true;
        return false;
    }

    @Override
    public List<RFileDTO> queryByHash(String hash) throws BusinessException {
        // 传入对象为空
        if (hash == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        List<RFile> rFiles = rFileMapper.selectList(new LambdaQueryWrapper<RFile>().eq(RFile::getHash, hash));
        return RFileConvert.INSTANCE.entity2dto(rFiles);
    }

    @Override
    public List<RFileDTO> queryByHash(String hash, Long size) throws BusinessException {
        // 传入对象为空
        if (hash == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        List<RFile> rFiles = rFileMapper.selectList(new LambdaQueryWrapper<RFile>()
                .eq(RFile::getHash, hash).eq(RFile::getSize,size));
        return RFileConvert.INSTANCE.entity2dto(rFiles);
    }
}
