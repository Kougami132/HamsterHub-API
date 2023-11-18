package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.AccountDTO;

public interface AccountService {
    AccountDTO create(AccountDTO accountDTO) throws BusinessException;
    void delete(Long accountId) throws BusinessException;
    void update(AccountDTO accountDTO) throws BusinessException;
    AccountDTO query(Long accountId) throws BusinessException;
    AccountDTO query(String username) throws BusinessException;
    Boolean isExist(Long accountId) throws BusinessException;
    Boolean isExist(String username) throws BusinessException;
    Boolean isAdmin(Long accountId) throws BusinessException;;
    Boolean isAdmin(String username) throws BusinessException;
}
