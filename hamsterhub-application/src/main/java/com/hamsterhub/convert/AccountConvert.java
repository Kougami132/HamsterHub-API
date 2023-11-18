package com.hamsterhub.convert;

import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.vo.AccountVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountConvert {
    AccountConvert INSTANCE = Mappers.getMapper(AccountConvert.class);

    AccountDTO vo2dto(AccountVO registerAccountVO);
    AccountVO dto2vo(AccountDTO accountDTO);
}
