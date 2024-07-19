package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.RFileDTO;

import java.util.List;

public interface RFileService {
    RFileDTO create(RFileDTO rFileDTO) throws BusinessException;

    RFileDTO createTemp(RFileDTO rFileDTO) throws BusinessException;

    void delete(Long rFileId) throws BusinessException;
    void update(RFileDTO rFileDTO) throws BusinessException;
    RFileDTO query(Long rFileId) throws BusinessException;
    RFileDTO query(String hash, Long strategyId) throws BusinessException;
    Boolean isExist(Long rFileId) throws BusinessException;
    Boolean isExist(String hash, Long strategyId) throws BusinessException;

    List<RFileDTO> queryByHash(String hash) throws BusinessException;

    List<RFileDTO> queryByHash(String hash, Long size) throws BusinessException;
}
