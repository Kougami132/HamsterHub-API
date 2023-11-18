package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.RFileDTO;

public interface RFileService {
    RFileDTO create(RFileDTO rFileDTO) throws BusinessException;
    void delete(Long rFileId) throws BusinessException;
    void update(RFileDTO rFileDTO) throws BusinessException;
    RFileDTO query(Long rFileId) throws BusinessException;
    Boolean isExist(Long rFileId) throws BusinessException;
    Boolean isExist(String hash) throws BusinessException;
}
