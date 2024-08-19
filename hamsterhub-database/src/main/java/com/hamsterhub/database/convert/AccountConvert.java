package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.AccountDTO;
import com.hamsterhub.database.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AccountConvert {
    AccountConvert INSTANCE = Mappers.getMapper(AccountConvert.class);

    AccountDTO entity2dto(Account account);
    Account dto2entity(AccountDTO accountDTO);

    List<AccountDTO> entity2dtoBatch(List<Account> accounts);
}
