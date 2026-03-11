package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountRequestDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountDTOMapper extends DTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {

    AccountDTOMapper INSTANCE =  Mappers.getMapper(AccountDTOMapper.class);

    @Override
    AccountDTO toDTO(AccountRequestDTO dto);

    @Override
    AccountResponseDTO toResponseDTO(AccountDTO accountDTO);
}