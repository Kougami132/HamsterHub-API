package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.convert.SysConfigConvert;
import com.hamsterhub.database.dto.SysConfigDTO;
import com.hamsterhub.database.entity.SysConfig;
import com.hamsterhub.database.mapper.SysConfigMapper;
import com.hamsterhub.database.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    List<SysConfigDTO> cache = null;

    @Override
    public void init(){
        if (!this.isExist(ConfigKey.CAN_REGISTER))
            sysConfigMapper.insert(new SysConfig(ConfigKey.CAN_REGISTER, "true", 0, "bool", false));
        if (!this.isExist(ConfigKey.PROXY_DANMA_BILI))
            sysConfigMapper.insert(new SysConfig(ConfigKey.PROXY_DANMA_BILI, "true", 0, "bool", false));
        if (!this.isExist(ConfigKey.JWT_SECRET_KEY))
            sysConfigMapper.insert(new SysConfig(ConfigKey.JWT_SECRET_KEY, UUID.randomUUID().toString(), 0, "str", true));
        if (!this.isExist(ConfigKey.BOT_GOCQ_URL))
            sysConfigMapper.insert(new SysConfig(ConfigKey.BOT_GOCQ_URL, "", 0, "str", true));
    }


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

    public Boolean isExist(String key){
        if(StringUtil.isBlank(key)){
            return false;
        }else{
            return sysConfigMapper.selectCount(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getKey, key)) > 0;
        }
    }

}
