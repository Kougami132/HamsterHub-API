package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.convert.ShareConvert;
import com.hamsterhub.service.dto.ShareDTO;
import com.hamsterhub.service.entity.Share;
import com.hamsterhub.service.mapper.ShareMapper;
import com.hamsterhub.service.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShareServiceImpl implements ShareService {

    @Autowired
    private ShareMapper shareMapper;

    @Override
    public ShareDTO create(ShareDTO shareDTO) throws BusinessException {
        // 传入对象为空
        if (shareDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);

        Share entity = ShareConvert.INSTANCE.dto2entity(shareDTO);
        shareMapper.insert(entity);

        return ShareConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long shareId) throws BusinessException {
        // 分享ID不存在
        if (!this.isExist(shareId))
            throw new BusinessException(CommonErrorCode.E_600011);
        shareMapper.deleteById(shareId);
    }

    @Override
    public void deleteByVFileId(Long vFileId) throws BusinessException {
        shareMapper.delete(new LambdaQueryWrapper<Share>().eq(Share::getVFileId, vFileId));
    }

    @Override
    public ShareDTO query(Long shareId) throws BusinessException {
        // 分享ID不存在
        if (!this.isExist(shareId))
            throw new BusinessException(CommonErrorCode.E_600011);
        return ShareConvert.INSTANCE.entity2dto(shareMapper.selectOne(new LambdaQueryWrapper<Share>().eq(Share::getId, shareId)));
    }

    @Override
    public ShareDTO query(String ticket) throws BusinessException {
        // 分享码不存在
        if (!this.isExist(ticket))
            throw new BusinessException(CommonErrorCode.E_600007);
        return ShareConvert.INSTANCE.entity2dto(shareMapper.selectOne(new LambdaQueryWrapper<Share>().eq(Share::getTicket, ticket)));
    }

    @Override
    public List<ShareDTO> queryBatch(Long AccountId) throws BusinessException {
        List<Share> shares = shareMapper.selectList(new LambdaQueryWrapper<Share>().eq(Share::getAccountID, AccountId));
        return ShareConvert.INSTANCE.entity2dtoBatch(shares);
    }

    @Override
    public Boolean isExist(Long shareId) throws BusinessException {
        return shareMapper.selectCount(new LambdaQueryWrapper<Share>().eq(Share::getId, shareId)) > 0;
    }

    @Override
    public Boolean isExistByVFileId(Long vFileId) throws BusinessException {
        return shareMapper.selectCount(new LambdaQueryWrapper<Share>().eq(Share::getVFileId, vFileId)) > 0;
    }

    @Override
    public Boolean isExist(String ticket) throws BusinessException {
        return shareMapper.selectCount(new LambdaQueryWrapper<Share>().eq(Share::getTicket, ticket)) > 0;
    }
}
