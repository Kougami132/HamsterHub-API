package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserDTO entity2dto(User user);
    User dto2entity(UserDTO userDTO);

    List<UserDTO> entity2dtoBatch(List<User> users);
}
