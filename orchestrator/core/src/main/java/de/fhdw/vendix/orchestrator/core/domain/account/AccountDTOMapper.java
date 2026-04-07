package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.AccountRequestDTO;
import de.fhdw.vendix.commons.api.domain.account.AccountResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface AccountDTOMapper extends DTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {

    @Override
    AccountDTO toDomainDTO(AccountRequestDTO dto);

    @Override
    AccountResponseDTO toResponseDTO(AccountDTO accountDTO);

    @Override
    Set<AccountDTO> toDomainDTOs(Iterable<AccountRequestDTO> requestDTOs);

    @Override
    Set<AccountResponseDTO> toResponseDTOs(Iterable<AccountDTO> domainDTOs);
}