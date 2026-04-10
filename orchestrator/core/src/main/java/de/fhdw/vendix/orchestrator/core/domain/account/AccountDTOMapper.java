package de.fhdw.vendix.orchestrator.core.domain.account;

import de.fhdw.vendix.commons.api.domain.account.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.AccountRequestDTO;
import de.fhdw.vendix.commons.api.domain.account.AccountResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface AccountDTOMapper extends DTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {

    @Override
    AccountDTO toDomainDTO(AccountRequestDTO dto);

    @Override
    AccountResponseDTO toResponseDTO(AccountDTO accountDTO);

    @Override
    List<AccountDTO> toDomainDTOs(Iterable<AccountRequestDTO> requestDTOs);

    @Override
    List<AccountResponseDTO> toResponseDTOs(Iterable<AccountDTO> domainDTOs);
}