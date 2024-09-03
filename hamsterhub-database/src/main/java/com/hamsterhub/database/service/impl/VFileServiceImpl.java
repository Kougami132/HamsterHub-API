package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.convert.VFileConvert;
import com.hamsterhub.database.dto.StrategyDTO;
import com.hamsterhub.database.dto.VFileDTO;
import com.hamsterhub.database.entity.VFile;
import com.hamsterhub.database.mapper.VFileMapper;
import com.hamsterhub.database.service.UserService;
import com.hamsterhub.database.service.StrategyService;
import com.hamsterhub.database.service.VFileService;
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
    private UserService userService;

    @Override
    public VFileDTO create(VFileDTO vFileDTO) throws BusinessException {
        // 传入对象为空
        if (vFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略不存在
        if (!strategyService.isExist(vFileDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        if (!vFileDTO.getParentId().equals(0L)) { // 不是根目录
            VFileDTO parentDTO = this.query(vFileDTO.getParentId());

            // 父目录不存在
            if (parentDTO == null)
                throw new BusinessException(CommonErrorCode.E_600003);
            // 父文件不为目录
            if (!parentDTO.isDir())
                throw new BusinessException(CommonErrorCode.E_600013);
        }
        // 文件已存在
        while (vFileMapper.selectCount(new LambdaQueryWrapper<VFile>()
                .eq(VFile::getStrategyId, vFileDTO.getStrategyId())
                .eq(VFile::getParentId, vFileDTO.getParentId())
                .eq(VFile::getName, vFileDTO.getName())) > 0)
            vFileDTO.setName(StringUtil.generateCopy(vFileDTO.getName()));

        VFile entity = VFileConvert.INSTANCE.dto2entity(vFileDTO);
        entity.setId(null);
        vFileMapper.insert(entity);
        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public VFileDTO createOverlay(VFileDTO vFileDTO) throws BusinessException {
        // 传入对象为空
        if (vFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略不存在
        if (!strategyService.isExist(vFileDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        if (!vFileDTO.getParentId().equals(0L)) { // 不是根目录
            VFileDTO parentDTO = this.query(vFileDTO.getParentId());

            // 父目录不存在
            if (parentDTO == null)
                throw new BusinessException(CommonErrorCode.E_600003);
            // 父文件不为目录
            if (!parentDTO.isDir())
                throw new BusinessException(CommonErrorCode.E_600013);
        }
        // 文件已存在
        while (vFileMapper.selectCount(new LambdaQueryWrapper<VFile>()
                .eq(VFile::getStrategyId, vFileDTO.getStrategyId())
                .eq(VFile::getParentId, vFileDTO.getParentId())
                .eq(VFile::getName, vFileDTO.getName())
                .eq(VFile::getType ,0))> 0)

            vFileDTO.setName(StringUtil.generateCopy(vFileDTO.getName()));

        VFile entity = VFileConvert.INSTANCE.dto2entity(vFileDTO);
        entity.setId(null);
        vFileMapper.insert(entity);
        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public VFileDTO createDir(VFileDTO vFileDTO) throws BusinessException {
        // 存储策略不存在
        if (!strategyService.isExist(vFileDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        if (!vFileDTO.getParentId().equals(0L)) { // 不是根目录
            VFileDTO parentDTO = this.query(vFileDTO.getParentId());

            // 父目录不存在
            if (parentDTO == null)
                throw new BusinessException(CommonErrorCode.E_600003);
            // 父文件不为目录
            if (!parentDTO.isDir())
                throw new BusinessException(CommonErrorCode.E_600013);
        }
        // 目录已存在
        while (vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getStrategyId, vFileDTO.getStrategyId()).eq(VFile::getParentId, vFileDTO.getParentId()).eq(VFile::getName, vFileDTO.getName())) > 0)
            vFileDTO.setName(StringUtil.generateCopy(vFileDTO.getName()));

        VFile entity = VFileConvert.INSTANCE.dto2entity(vFileDTO);
        entity.setId(null);
        vFileMapper.insert(entity);
        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<String> delete(Long vFileId) throws BusinessException {
        // 传入对象为空
        if (vFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(vFileId))
            throw new BusinessException(CommonErrorCode.E_600001);

        VFileDTO vFileDTO = this.query(vFileId);
        List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>()
                .eq(VFile::getUserId, vFileDTO.getUserId())
                .eq(VFile::getStrategyId, vFileDTO.getStrategyId())
                .eq(VFile::getParentId, vFileDTO.getParentId())
                .eq(VFile::getName, vFileDTO.getName()));
        List<String> result = new ArrayList<>();
        for (VFile i: vFiles)
            this.rm(result, i.getId());
        return result;
    }

    @Override
    public void update(VFileDTO vFileDTO) throws BusinessException {
        // 传入对象为空
        if (vFileDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(Long.parseLong(vFileDTO.getId())))
            throw new BusinessException(CommonErrorCode.E_600001);
        // 存储策略不存在
        if (!strategyService.isExist(vFileDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        // 父目录不存在
        if (!vFileDTO.getParentId().equals(0L) && !this.isExist(vFileDTO.getParentId()))
            throw new BusinessException(CommonErrorCode.E_600003);
        // 父文件不为目录
        if (!vFileDTO.getParentId().equals(0L) && !this.query(vFileDTO.getParentId()).getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600013);

        VFile entity = VFileConvert.INSTANCE.dto2entity(vFileDTO);
        vFileMapper.updateById(entity);
    }

    @Override
    public VFileDTO query(Long vFileId) throws BusinessException {
        // 传入对象为空
        if (vFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);

        VFile entity = vFileMapper.selectById(vFileId);

        // 文件不存在
        if (entity == null){
            throw new BusinessException(CommonErrorCode.E_600001);
        }

        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public VFileDTO query(Long vFileId, Long userId) throws BusinessException {
        // 传入对象为空
        if (vFileId == null)
            throw new BusinessException(CommonErrorCode.E_100001);

        LambdaQueryWrapper<VFile> wrapper = new LambdaQueryWrapper<VFile>()
                .eq(VFile::getUserId, userId)
                .eq(VFile::getId, vFileId);

        VFile entity = vFileMapper.selectOne(wrapper);

        // 文件不存在
        if (entity == null){
            throw new BusinessException(CommonErrorCode.E_600001);
        }

        return VFileConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public VFileDTO query(Long strategyId, Long parentId, String name) throws BusinessException {
        // 传入对象为空
        if (parentId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 文件不存在
        if (!this.isExist(parentId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile parent = vFileMapper.selectById(parentId);
        // 文件不是目录
        if (!parent.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);

        // 文件不存在
        if (vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getStrategyId, strategyId).eq(VFile::getParentId, parentId).eq(VFile::getName, name)) == 0)
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile vFile = vFileMapper.selectOne(new LambdaQueryWrapper<VFile>().eq(VFile::getStrategyId, strategyId).eq(VFile::getParentId, parentId).eq(VFile::getName, name));
        return VFileConvert.INSTANCE.entity2dto(vFile);
    }

    @Override
    public List<VFileDTO> query(Long userId, Long strategyId, Long parentId, String name) throws BusinessException {
        // 传入对象为空
        if (userId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户不存在
        if (!userService.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 文件不存在
        if (!this.isExist(userId, strategyId, parentId, name))
            throw new BusinessException(CommonErrorCode.E_600001);

        // 取出版本最新的VFile
        List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().
                eq(VFile::getUserId, userId).
                eq(VFile::getStrategyId, strategyId).
                eq(VFile::getParentId, parentId).
                eq(VFile::getName, name));
        vFiles.sort((o1, o2) -> o2.getVersion() - o1.getVersion());
        return VFileConvert.INSTANCE.entity2dtoBatch(vFiles);
    }

    @Override
    public List<VFileDTO> query(Long userId, String root, Long parentId, String name) throws BusinessException {
        // 传入对象为空
        if (userId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户不存在
        if (!userService.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 文件不存在
        if (!this.isExist(userId, root, parentId, name))
            throw new BusinessException(CommonErrorCode.E_600001);

        StrategyDTO strategyDTO = strategyService.query(root);
        // 取出版本最新的VFile
        List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getUserId, userId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getParentId, parentId).eq(VFile::getName, name));
        vFiles.sort((o1, o2) -> o2.getVersion() - o1.getVersion());
        return VFileConvert.INSTANCE.entity2dtoBatch(vFiles);
    }

    @Override
    public List<VFileDTO> queryBatch(Long parentId) throws BusinessException {
        // 目录不存在
        if (parentId != 0 && !this.isExist(parentId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile entity = vFileMapper.selectById(parentId);
        // 文件不是目录
        if (!entity.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);

        List<VFile> entities = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getParentId, parentId));
        return VFileConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public List<VFileDTO> queryBatch(Long parentId, Integer page, Integer limit) throws BusinessException {
        // 目录不存在
        if (parentId != 0 && !this.isExist(parentId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile entity = vFileMapper.selectById(parentId);
        // 文件不是目录
        if (!entity.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);

        Page<VFile> rowPage = new Page(page, limit);
        List<VFile> entities = vFileMapper.selectPage(rowPage, new LambdaQueryWrapper<VFile>().eq(VFile::getParentId, parentId)).getRecords();
        return VFileConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public List<VFileDTO> queryBatch(Long userId, Long strategyId, Long parentId) throws BusinessException {
        // 用户不存在
        if (!userService.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 目录不存在
        if (parentId != 0 && !this.isExist(parentId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile entity = vFileMapper.selectById(parentId);
        // 文件不是目录
        if (!parentId.equals(0L) && !entity.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);

        List<VFile> entities = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getUserId, userId).eq(VFile::getStrategyId, strategyId).eq(VFile::getParentId, parentId));
        return VFileConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public List<VFileDTO> queryBatch(Long userId, String root, Long parentId) throws BusinessException {
        // 传入对象为空
        if (userId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户不存在
        if (!userService.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 目录不存在
        if (parentId != 0 && !this.isExist(parentId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile entity = vFileMapper.selectById(parentId);
        // 文件不是目录
        if (!parentId.equals(0L) && !entity.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);

        StrategyDTO strategyDTO = strategyService.query(root);
        List<VFile> entities = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getUserId, userId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getParentId, parentId));
        return VFileConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public List<VFileDTO> queryBatch(Long userId, String root, Long parentId, Integer page, Integer limit) throws BusinessException {
        // 用户不存在
        if (!userService.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 目录不存在
        if (parentId != 0 && !this.isExist(parentId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile entity = vFileMapper.selectById(parentId);
        // 文件不是目录
        if (!parentId.equals(0L) && !entity.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);

        StrategyDTO strategyDTO = strategyService.query(root);

        Page<VFile> rowPage = new Page(page, limit);
        List<VFile> entities = vFileMapper.selectPage(rowPage, new LambdaQueryWrapper<VFile>().eq(VFile::getUserId, userId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getParentId, parentId)).getRecords();
        return VFileConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public Integer queryCount(Long vFileId) throws BusinessException {
        // 目录不存在
        if (vFileId != 0 && !this.isExist(vFileId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFile entity = vFileMapper.selectById(vFileId);
        // 文件不是目录
        if (!entity.getType().equals(0))
            throw new BusinessException(CommonErrorCode.E_600003);
        List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getParentId, vFileId));
//        // 去重
//        List<String> names = vFiles.stream()
//                                    .map(VFile::getName)
//                                    .distinct()
//                                    .collect(Collectors.toList());
        return vFiles.size();
    }

    @Override
    public Boolean isExist(Long vFileId) throws BusinessException {
        return vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getId, vFileId)) > 0;
    }

    @Override
    public Boolean isExist(Long userId, Long strategyId, Long parentId, String name) throws BusinessException {
        return vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getUserId, userId).eq(VFile::getStrategyId, strategyId).eq(VFile::getParentId, parentId).eq(VFile::getName, name)) > 0;
    }

    @Override
    public Boolean isExist(Long userId, String root, Long parentId, String name) throws BusinessException {
        StrategyDTO strategyDTO = strategyService.query(root);
        return vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getUserId, userId).eq(VFile::getStrategyId, strategyDTO.getId()).eq(VFile::getParentId, parentId).eq(VFile::getName, name)) > 0;
    }

    @Override
    public void rename(Long vFileId, String newName) throws BusinessException {
        // 文件不存在
        if (!this.isExist(vFileId))
            throw new BusinessException(CommonErrorCode.E_600001);
        VFileDTO vFileDTO = this.query(vFileId);
        // 新文件名已存在
        if (this.isExist(vFileDTO.getUserId(), vFileDTO.getStrategyId(), vFileDTO.getParentId(), newName))
            throw new BusinessException(CommonErrorCode.E_600015);

        // 所有版本的VFile都重命名
        LambdaUpdateWrapper<VFile> updateWrapper = new LambdaUpdateWrapper<VFile>().eq(VFile::getUserId, vFileDTO.getUserId()).eq(VFile::getStrategyId, vFileDTO.getStrategyId()).eq(VFile::getParentId, vFileDTO.getParentId()).eq(VFile::getName, vFileDTO.getName()).set(VFile::getName, newName);
        vFileMapper.update(null, updateWrapper);
    }

    @Override
    public Long getShareParent(Long vFileId) throws BusinessException {
        VFileDTO vFileDTO = this.query(vFileId);
        while (vFileDTO.getShareType().equals(0)) {
            if (vFileDTO.getParentId().equals(0L)) // 父节点为根目录
                return 0L;
            vFileDTO = this.query(vFileDTO.getParentId());
        }
        if (vFileDTO.getShareType().equals(1))
            return Long.parseLong(vFileDTO.getId());
        else if (vFileDTO.getShareType().equals(2)) {
            return 2L;
        } else
            return 0L;
    }

//    private void rm(List<Long> result, Long vFileId) {
//        VFile entity = vFileMapper.selectById(vFileId);
//        if (entity.getType().equals(0)) { //文件夹
//            Long userId = entity.getUserId();
//            Long parentId = entity.getParentId();
//            List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>().eq(VFile::getUserID, userId).like(VFile::getParentId, vFileId));
//            for (VFile vfile: vFiles)
//                rm(result, vfile.getId());
//        }
//        vFileMapper.deleteById(vFileId);
//        // 删除实际文件，条件：文件为文件类型且无相同rFile指向
//        if (entity.getType().equals(1) && vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getRFileId, entity.getRFileId())) == 0)
//            result.add(entity.getRFileId());
//    }

    private void rm(List<String> result, Long vFileId) {
        VFile entity = vFileMapper.selectById(vFileId);
        if (entity.getType().equals(0)) { //文件夹
            Long userId = entity.getUserId();
            Long parentId = entity.getParentId();
            List<VFile> vFiles = vFileMapper.selectList(new LambdaQueryWrapper<VFile>()
                    .eq(VFile::getUserId, userId)
                    .like(VFile::getParentId, vFileId));
            for (VFile vfile: vFiles)
                rm(result, vfile.getId());
        }
        vFileMapper.deleteById(vFileId);
        // 删除实际文件，条件：文件为文件类型且无相同rFile指向
        if (entity.getType().equals(1) && vFileMapper.selectCount(new LambdaQueryWrapper<VFile>().eq(VFile::getHash, entity.getHash())) == 0)
            result.add(entity.getHash());
    }

}
