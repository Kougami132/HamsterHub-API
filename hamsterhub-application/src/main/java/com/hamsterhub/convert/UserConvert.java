package com.hamsterhub.convert;

import com.hamsterhub.response.UserResponse;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.DeviceDTO;
import com.hamsterhub.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserDTO vo2dto(UserVO UserVO);
    UserVO dto2vo(UserDTO userDTO);

    UserResponse dto2res(UserDTO userDTO);

    List<UserResponse> dto2resBatch(List<UserDTO> userDTOs);
}
