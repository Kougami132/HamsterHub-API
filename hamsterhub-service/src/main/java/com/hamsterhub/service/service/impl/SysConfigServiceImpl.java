package com.hamsterhub.service.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.convert.SysConfigConvert;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.entity.SysConfig;
import com.hamsterhub.service.mapper.SysConfigMapper;
import com.hamsterhub.service.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    List<SysConfigDTO> cache = null;


    @Override
    public void set(SysConfigDTO sysConfigDTO) throws BusinessException {
        // 传入对象为空
        if (sysConfigDTO == null || sysConfigDTO.getKey() == null)
            throw new BusinessException(CommonErrorCode.E_100001);

        // key为空
        if (StringUtil.isBlank(sysConfigDTO.getKey()))
            throw new BusinessException(CommonErrorCode.E_800001);


        SysConfig entity = SysConfigConvert.INSTANCE.dto2entity(sysConfigDTO);
        sysConfigMapper.updateById(entity);

        // 使缓存失效
        this.cache = null;
    }

    @Override
    public List<SysConfigDTO> query() throws BusinessException {
        // 缓存
        if(this.cache == null){
            List<SysConfig>  entities = sysConfigMapper.selectList(null);
            this.cache = SysConfigConvert.INSTANCE.entity2dtoBatch(entities);
        }

        return this.cache;
    }

}
