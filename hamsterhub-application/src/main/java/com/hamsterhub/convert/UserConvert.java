package com.hamsterhub.convert;

import com.hamsterhub.response.DeviceResponse;
import com.hamsterhub.response.UserResponse;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.vo.DeviceVO;
import com.hamsterhub.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    AccountDTO vo2dto(UserVO UserVO);
    UserVO dto2vo(AccountDTO accountDTO);

    UserResponse dto2res(AccountDTO accountDTO);

    List<UserResponse> dto2resBatch(List<AccountDTO> accountDTOs);
}
