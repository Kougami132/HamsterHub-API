package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.service.convert.VFileConvert;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.VFile;
import com.hamsterhub.service.mapper.VFileMapper;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.service.service.StrategyService;
import com.hamsterhub.service.service.VFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VFileServiceImpl implements VFileService {

    @Autowired
    private VFileMapper vFileMapper;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private AccountService accountService;

    @Override
    public VFileDTO create(VFileDTO vFileDTO) throws BusinessException {
        // 传入对象为空
        if (vFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略不存在
        if (!strategyService.isExist(vFileDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        // 路径格式错误
        if (!MatchUtil.isPathMatches(vFileDTO.getPath()))
            throw new BusinessException(CommonErrorCode.E_600002);

        VFile entity = VFileConvert.INSTANCE.dto2entity(vFileDTO);
        entity.setId(null);
        vFileMapper.insert(entity);
        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<Long> delete(Long vFileId) throws BusinessException {
        // 传入对象为空
        if (vFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(vFileId))
            throw new BusinessException(CommonErrorCode.E_600001);

        List<Long> result = new ArrayList<>();
        this.rm(result, vFileId);
        return result;
    }

    @Override
    public void update(VFileDTO vFileDTO) throws BusinessException {
        // 传入对象为空
        if (vFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(vFileDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600001);
        // 存储策略不存在
        if (!strategyService.isExist(vFileDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        // 路径格式错误
        if (!MatchUtil.isPathMatches(vFileDTO.getPath()))
            throw new BusinessException(CommonErrorCode.E_600002);

        VFile entity = VFileConvert.INSTANCE.dto2entity(vFileDTO);
        vFileMapper.updateById(entity);
    }

    @Override
    public VFileDTO query(Long vFileId) throws BusinessException {
        // 传入对象为空
        if (vFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(vFileId))
            throw new BusinessException(CommonErrorCode.E_600001);

        VFile entity = vFileMapper.selectById(vFileId);
        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public VFileDTO query(Long accountId, String root, String path, String name) throws BusinessException {
        // 传入对象为空
        if (accountId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户不存在
        if (!accountService.isExist(accountId))
            throw new BusinessException(CommonErrorCode.E_200013);

        StrategyDTO strategyDTO = strategyService.query(root);
        VFile entity = vFileMapper.selectOne(new LambdaQueryWrapper<VFile>().eq(VFile::getAccountID, accountId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getPath, path).eq(VFile::getName, name));
        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<Long> deleteBatch(Long accountId, List<Long> vFileIds) throws BusinessException {
        // 传入对象为空
        if (accountId == null || vFileIds == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户不存在
        if (!accountService.isExist(accountId))
            throw new BusinessException(CommonErrorCode.E_200013);

        List<Long> result = new ArrayList<>();
        for (Long vFileId: vFileIds)
            if (vFileMapper.selectOne(new LambdaQueryWrapper<VFile>().eq(VFile::getId, vFileId)).getAccountID().equals(accountId))
                this.rm(result, vFileId);
        return result;
    }

    @Override
    public List<VFileDTO> queryBatch(Long accountId, String root, String path) throws BusinessException {
        // 传入对象为空
        if (accountId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户不存在
        if (!accountService.isExist(accountId))
            throw new BusinessException(CommonErrorCode.E_200013);

        StrategyDTO strategyDTO = strategyService.query(root);
        List<VFile> entities = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getAccountID, accountId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getPath, path));
        return VFileConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public Boolean isExist(Long vFileId) throws BusinessException {
        return vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getId, vFileId)) > 0;
    }

    @Override
    public Boolean isExist(Long accountId, String root, String path, String name) throws BusinessException {
        StrategyDTO strategyDTO = strategyService.query(root);
        return vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getAccountID, accountId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getPath, path).eq(VFile::getName, name)) > 0;
    }

    public void rm(List<Long> result, Long vFileId) {
        VFile entity = vFileMapper.selectById(vFileId);
        if (entity.getType().equals(0)) { //文件夹
            Long accountId = entity.getAccountID();
            String path = entity.getPath();
            List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getAccountID, accountId).like(VFile::getPath, path));
            for (VFile vfile: vFiles)
                rm(result, vfile.getId());
        }
        vFileMapper.deleteById(vFileId);
        // 删除实际文件，条件：文件为文件类型且无相同rFile指向
        if (entity.getType().equals(1) && vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getRFileId, entity.getRFileId())) == 0)
            result.add(vFileId);
    }
}
