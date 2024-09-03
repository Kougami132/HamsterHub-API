package com.hamsterhub.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.convert.UserConvert;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.entity.User;
import com.hamsterhub.database.mapper.UserMapper;
import com.hamsterhub.database.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void init() throws BusinessException {
        if (!this.isExist(0L)) {
            User admin = new User();
            admin.setId(0L);
            admin.setUsername("admin");
            admin.setPassword(MD5Util.getMd5("admin132"));
            admin.setPassModified(LocalDateTime.now());
            admin.setType(0);
            userMapper.insert(admin);
        }
    }

    @Override
    public UserDTO create(UserDTO userDTO) throws BusinessException {
        // 传入对象为空
        if (userDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 用户名为空
        if (StringUtil.isBlank(userDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200001);
        // 密码为空
        if (StringUtil.isBlank(userDTO.getPassword()))
            throw new BusinessException(CommonErrorCode.E_200002);
        // 用户名格式错误
        if (!MatchUtil.isUsernameMatches(userDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200004);
        // 密码格式错误
        if (!MatchUtil.isPasswordMatches(userDTO.getPassword()))
            throw new BusinessException(CommonErrorCode.E_200005);
        // 用户名已存在
        if (this.isExist(userDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200007);
        // 手机号已存在
        if (this.isPhoneExist(userDTO.getPhone()))
            throw new BusinessException(CommonErrorCode.E_200008);

        // 密码MD5加密
        userDTO.setPassword(MD5Util.getMd5(userDTO.getPassword()));

        User entity = UserConvert.INSTANCE.dto2entity(userDTO);
        entity.setId(null);
        userMapper.insert(entity);
        return UserConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long userId) throws BusinessException {
        // 传入对象为空
        if (userId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);

        userMapper.deleteById(userId);
    }

    @Override
    public void update(UserDTO userDTO) throws BusinessException {
        // 传入对象为空
        if (userDTO == null || userDTO.getId() == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(userDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 用户名格式错误
        if (!MatchUtil.isUsernameMatches(userDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200004);
        // 邮箱格式错误
        if (!StringUtil.isBlank(userDTO.getEmail()) && !MatchUtil.isEmailMatches(userDTO.getEmail()))
            throw new BusinessException(CommonErrorCode.E_200014);
        // 密码格式错误
        if (!MatchUtil.isPasswordMatches(userDTO.getPassword()))
            throw new BusinessException(CommonErrorCode.E_200005);
        // 用户名已存在
        Wrapper exist = new LambdaQueryWrapper<User>().eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(exist) > 0 && !userMapper.selectOne(exist).getId().equals(userDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200007);
        // 手机号已存在
        exist = new LambdaQueryWrapper<User>().eq(User::getPhone, userDTO.getPhone());
        if (userMapper.selectCount(exist) > 0 && !userMapper.selectOne(exist).getId().equals(userDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200008);

        User entity = UserConvert.INSTANCE.dto2entity(userDTO);
        userMapper.updateById(entity);
    }

    @Override
    public void updateForAdmin(UserDTO userDTO) throws BusinessException {
        // 传入对象为空
        if (userDTO == null || userDTO.getId() == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(userDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200013);
        // 用户名格式错误
        if (!MatchUtil.isUsernameMatches(userDTO.getUsername()))
            throw new BusinessException(CommonErrorCode.E_200004);
        // 邮箱格式错误
        if (!StringUtil.isBlank(userDTO.getEmail()) && !MatchUtil.isEmailMatches(userDTO.getEmail()))
            throw new BusinessException(CommonErrorCode.E_200014);
        // 密码格式错误
        if (userDTO.getPassword()!=null)
            if(!MatchUtil.isPasswordMatches(userDTO.getPassword()))
                throw new BusinessException(CommonErrorCode.E_200005);
        // 用户名已存在
        Wrapper exist = new LambdaQueryWrapper<User>().eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(exist) > 0 && !userMapper.selectOne(exist).getId().equals(userDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_200007);

        User entity = UserConvert.INSTANCE.dto2entity(userDTO);
        userMapper.updateById(entity);
    }

    @Override
    public UserDTO query(Long userId) throws BusinessException {
        // 传入对象为空
        if (userId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 账户id不存在
        if (!this.isExist(userId))
            throw new BusinessException(CommonErrorCode.E_200013);

        User entity = userMapper.selectById(userId);
        return UserConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public UserDTO query(String username) throws BusinessException {
        // 传入对象为空
        if (StringUtil.isBlank(username))
            throw new BusinessException(CommonErrorCode.E_100001);

        User entity = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (entity == null){
            throw new BusinessException(CommonErrorCode.E_200015);
        }

        return UserConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public Boolean isExist(Long userId) throws BusinessException {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getId, userId)) > 0;
    }

    @Override
    public Boolean isExist(String username) throws BusinessException {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    @Override
    public Boolean isPhoneExist(Long phone) throws BusinessException {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0;
    }

    @Override
    public List<UserDTO> FetchAll() throws BusinessException{
        List<User> entities = userMapper.selectList(null);
        return UserConvert.INSTANCE.entity2dtoBatch(entities);
    }
}
