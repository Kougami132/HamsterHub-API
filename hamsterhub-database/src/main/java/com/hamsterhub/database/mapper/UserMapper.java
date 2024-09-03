package com.hamsterhub.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hamsterhub.database.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {

}