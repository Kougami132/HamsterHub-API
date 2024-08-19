package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.SysConfigDTO;
import com.hamsterhub.database.dto.VFileDTO;

import java.util.List;

public interface SysConfigService {

    void init();

    void set(SysConfigDTO sysConfigDTO) throws BusinessException;

    List<SysConfigDTO> query() throws BusinessException;
}
