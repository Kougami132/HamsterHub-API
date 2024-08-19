package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.StrategyDTO;

import java.util.List;

public interface StrategyService {
    StrategyDTO create(StrategyDTO strategyDTO) throws BusinessException;
    void delete(Long strategyId) throws BusinessException;
    void update(StrategyDTO strategyDTO) throws BusinessException;
    StrategyDTO query(Long strategyId) throws BusinessException;
    StrategyDTO query(String root) throws BusinessException;
    List<StrategyDTO> queryBatch() throws BusinessException;
    Boolean isExist(Long strategyId) throws BusinessException;
}
