package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.convert.StrategyConvert;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.entity.Strategy;
import com.hamsterhub.service.mapper.StrategyMapper;
import com.hamsterhub.service.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    private StrategyMapper strategyMapper;

    @Override
    public StrategyDTO create(StrategyDTO strategyDTO) throws BusinessException {
        // 传入对象为空
        if (strategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);

        Strategy entity = StrategyConvert.INSTANCE.dto2entity(strategyDTO);
        entity.setId(null);
        strategyMapper.insert(entity);
        return StrategyConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long strategyId) throws BusinessException {
        // 传入对象为空
        if (strategyId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储设备不存在
        if (this.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        strategyMapper.deleteById(strategyId);
    }

    @Override
    public void update(StrategyDTO strategyDTO) throws BusinessException {
        // 传入对象为空
        if (strategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略已存在
        Wrapper exist = new LambdaQueryWrapper<Strategy>().eq(Strategy::getName, strategyDTO.getName());
        if (strategyMapper.selectCount(exist) > 0 && !strategyMapper.selectOne(exist).getId().equals(strategyDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_400002);

        Strategy entity = StrategyConvert.INSTANCE.dto2entity(strategyDTO);
        strategyMapper.updateById(entity);
    }

    @Override
    public StrategyDTO query(Long strategyId) throws BusinessException {
        // 传入对象为空
        if (strategyId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略不存在
        if (this.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        Strategy entity = strategyMapper.selectById(strategyId);
        return StrategyConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<StrategyDTO> queryBatch() throws BusinessException {
        List<Strategy> entities = strategyMapper.selectList(null);
        return StrategyConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public Boolean isExist(Long strategyId) throws BusinessException {
        return strategyMapper.selectCount(new LambdaQueryWrapper<Strategy>().eq(Strategy::getId, strategyId)) > 0;
    }

}
