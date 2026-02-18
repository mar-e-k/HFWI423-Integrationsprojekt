package de.fhdw.vendix.commons.api.account;

import de.fhdw.vendix.commons.api.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.account.dto.AccountRequestDTO;
import de.fhdw.vendix.commons.api.account.dto.AccountResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface AccountDtoMapper {
    AccountRequestDTO toRequestDTO(AccountDTO dto);

    AccountResponseDTO toResponseDTO(AccountDTO dto);
}