package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.convert.PushConfigConvert;
import com.hamsterhub.database.dto.PushConfigDTO;
import com.hamsterhub.database.entity.PushConfig;
import com.hamsterhub.database.mapper.PushConfigMapper;
import com.hamsterhub.database.service.PushConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PushConfigServiceImpl implements PushConfigService {

    @Autowired
    private PushConfigMapper pushConfigMapper;

    @Override
    public PushConfigDTO create(PushConfigDTO pushConfigDTO) throws BusinessException {
        PushConfig entity = PushConfigConvert.INSTANCE.dto2entity(pushConfigDTO);
        pushConfigMapper.insert(entity);
        return PushConfigConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long userId) throws BusinessException {
        pushConfigMapper.delete(new LambdaQueryWrapper<PushConfig>()
                                .eq(PushConfig::getUserId, userId));
    }

    @Override
    public PushConfigDTO query(Long userId) throws BusinessException {
        PushConfig pushConfig = pushConfigMapper.selectOne(new LambdaQueryWrapper<PushConfig>()
                                                                    .eq(PushConfig::getUserId, userId));
        return PushConfigConvert.INSTANCE.entity2dto(pushConfig);
    }

    @Override
    public void update(PushConfigDTO pushConfigDTO) throws BusinessException {
        pushConfigMapper.update(PushConfigConvert.INSTANCE.dto2entity(pushConfigDTO),
                                new LambdaQueryWrapper<PushConfig>()
                                .eq(PushConfig::getUserId, pushConfigDTO.getUserId()));
    }

    @Override
    public Boolean isExist(Long userId) throws BusinessException {
        return pushConfigMapper.exists(new LambdaQueryWrapper<PushConfig>()
                                        .eq(PushConfig::getUserId, userId));
    }
}
