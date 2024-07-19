package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.convert.AccountConvert;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.entity.Account;
import com.hamsterhub.service.mapper.AccountMapper;
import com.hamsterhub.service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public void init() throws BusinessException {
        if (!this.isExist(0L)) {
            Account admin = new Account();
            admin.setId(0L);
            admin.setUsername("admin");
            admin.setPassword(MD5Util.getMd5("admin132"));
            admin.setPassModified(LocalDateTime.now());
            admin.setType(0);
            accountMapper.insert(admin);
        }
    }

    @Override
    public AccountDTO create(AccountDTO accountDTO) throws BusinessException {
        // 传入对象为空
        if (accountDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户名为空
        if (StringUtil.isBlank(accountDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200001);
        // 密码为空
        if (StringUtil.isBlank(accountDTO.getPassword()))
            throw new BusinessException(CommonErrorCode.E_200002);
        // 用户名格式错误
        if (!MatchUtil.isUsernameMatches(accountDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200004);
        // 密码格式错误
        if (!MatchUtil.isPasswordMatches(accountDTO.getPassword()))
            throw new BusinessException(CommonErrorCode.E_200005);
        // 用户名已存在
        if (this.isExist(accountDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200007);
        // 手机号已存在
        if (this.isPhoneExist(accountDTO.getPhone()))
            throw new BusinessException(CommonErrorCode.E_200008);

        // 密码MD5加密
        accountDTO.setPassword(MD5Util.getMd5(accountDTO.getPassword()));

        Account entity = AccountConvert.INSTANCE.dto2entity(accountDTO);
        entity.setId(null);
        accountMapper.insert(entity);
        return AccountConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long accountId) throws BusinessException {
        // 传入对象为空
        if (accountId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(accountId))
            throw new BusinessException(CommonErrorCode.E_200013);

        accountMapper.deleteById(accountId);
    }

    @Override
    public void update(AccountDTO accountDTO) throws BusinessException {
        // 传入对象为空
        if (accountDTO == null || accountDTO.getId() == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 用户名格式错误
        if (!MatchUtil.isUsernameMatches(accountDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200004);
        // 邮箱格式错误
        if (!StringUtil.isBlank(accountDTO.getEmail()) && !MatchUtil.isEmailMatches(accountDTO.getEmail()))
            throw new BusinessException(CommonErrorCode.E_200014);
        // 密码格式错误
        if (!MatchUtil.isPasswordMatches(accountDTO.getPassword()))
            throw new BusinessException(CommonErrorCode.E_200005);
        // 用户名已存在
        Wrapper exist = new LambdaQueryWrapper<Account>().eq(Account::getUsername, accountDTO.getUsername());
        if (accountMapper.selectCount(exist) > 0 && !accountMapper.selectOne(exist).getId().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200007);
        // 手机号已存在
        if (this.isPhoneExist(accountDTO.getPhone()))
            throw new BusinessException(CommonErrorCode.E_200008);

        Account entity = AccountConvert.INSTANCE.dto2entity(accountDTO);
        accountMapper.updateById(entity);
    }

    @Override
    public void updateForAdmin(AccountDTO accountDTO) throws BusinessException {
        // 传入对象为空
        if (accountDTO == null || accountDTO.getId() == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 用户名格式错误
        if (!MatchUtil.isUsernameMatches(accountDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200004);
        // 邮箱格式错误
        if (!StringUtil.isBlank(accountDTO.getEmail()) && !MatchUtil.isEmailMatches(accountDTO.getEmail()))
            throw new BusinessException(CommonErrorCode.E_200014);
        // 密码格式错误
        if (accountDTO.getPassword()!=null)
            if(!MatchUtil.isPasswordMatches(accountDTO.getPassword()))
                throw new BusinessException(CommonErrorCode.E_200005);
        // 用户名已存在
        Wrapper exist = new LambdaQueryWrapper<Account>().eq(Account::getUsername, accountDTO.getUsername());
        if (accountMapper.selectCount(exist) > 0 && !accountMapper.selectOne(exist).getId().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200007);

        Account entity = AccountConvert.INSTANCE.dto2entity(accountDTO);
        accountMapper.updateById(entity);
    }

    @Override
    public AccountDTO query(Long accountId) throws BusinessException {
        // 传入对象为空
        if (accountId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(accountId))
            throw new BusinessException(CommonErrorCode.E_200013);

        Account entity = accountMapper.selectById(accountId);
        return AccountConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public AccountDTO query(String username) throws BusinessException {
        // 传入对象为空
        if (StringUtil.isBlank(username))
            throw new BusinessException(CommonErrorCode.E_100001);

        Account entity = accountMapper.selectOne(new LambdaQueryWrapper<Account>().eq(Account::getUsername, username));

        if (entity == null){
            throw new BusinessException(CommonErrorCode.E_200015);
        }

        return AccountConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public Boolean isExist(Long accountId) throws BusinessException {
        return accountMapper.selectCount(new LambdaQueryWrapper<Account>().eq(Account::getId, accountId)) > 0;
    }

    @Override
    public Boolean isExist(String username) throws BusinessException {
        return accountMapper.selectCount(new LambdaQueryWrapper<Account>().eq(Account::getUsername, username)) > 0;
    }

    @Override
    public Boolean isPhoneExist(Long phone) throws BusinessException {
        return accountMapper.selectCount(new LambdaQueryWrapper<Account>().eq(Account::getPhone, phone)) > 0;
    }

    @Override
    public List<AccountDTO> FetchAll() throws BusinessException{
        List<Account> entities = accountMapper.selectList(null);
        return AccountConvert.INSTANCE.entity2dtoBatch(entities);
    }
}
