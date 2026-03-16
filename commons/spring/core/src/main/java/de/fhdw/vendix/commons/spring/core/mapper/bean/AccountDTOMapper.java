package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountRequestDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapperConfig.class)
public interface AccountDTOMapper extends DTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {

    @Override
    AccountDTO toDomainDTO(AccountRequestDTO dto);

    @Override
    AccountResponseDTO toResponseDTO(AccountDTO accountDTO);
}