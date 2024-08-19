package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.database.convert.StrategyConvert;
import com.hamsterhub.database.dto.StrategyDTO;
import com.hamsterhub.database.entity.Strategy;
import com.hamsterhub.database.mapper.StrategyMapper;
import com.hamsterhub.database.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    private StrategyMapper strategyMapper;

    @Override
    public StrategyDTO create(StrategyDTO strategyDTO) throws BusinessException {
        // 传入对象为空
        if (strategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略名称已存在
        if (strategyMapper.selectCount(new LambdaQueryWrapper<Strategy>().eq(Strategy::getName, strategyDTO.getName())) > 0)
            throw new BusinessException(CommonErrorCode.E_400002);
        // 存储策略根目录已存在
        if (strategyMapper.selectCount(new LambdaQueryWrapper<Strategy>().eq(Strategy::getRoot, strategyDTO.getRoot())) > 0)
            throw new BusinessException(CommonErrorCode.E_400006);

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
        if (!this.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        strategyMapper.deleteById(strategyId);
    }

    @Override
    public void update(StrategyDTO strategyDTO) throws BusinessException {
        // 传入对象为空
        if (strategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储策略名称已存在
        Wrapper exist = new LambdaQueryWrapper<Strategy>().eq(Strategy::getName, strategyDTO.getName());
        if (strategyMapper.selectCount(exist) > 0 && !strategyMapper.selectOne(exist).getId().equals(strategyDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_400002);
        // 存储策略根目录已存在
        Wrapper exist2 = new LambdaQueryWrapper<Strategy>().eq(Strategy::getRoot, strategyDTO.getRoot());
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
        if (!this.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        Strategy entity = strategyMapper.selectById(strategyId);
        return StrategyConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public StrategyDTO query(String root) throws BusinessException {
        Strategy strategyDTO = strategyMapper.selectOne(new LambdaQueryWrapper<Strategy>().eq(Strategy::getRoot, root));
        if (strategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_400001);
        return StrategyConvert.INSTANCE.entity2dto(strategyDTO);
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
