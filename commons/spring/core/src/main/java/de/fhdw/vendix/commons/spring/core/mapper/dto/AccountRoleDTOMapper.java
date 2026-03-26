package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleRequestDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface AccountRoleDTOMapper extends DTOMapper<AccountRoleDTO, AccountRoleRequestDTO, AccountRoleResponseDTO> {

    @Override
    AccountRoleDTO toDomainDTO(AccountRoleRequestDTO dto);

    @Override
    AccountRoleResponseDTO toResponseDTO(AccountRoleDTO accountRoleDTO);

    @Override
    List<AccountRoleDTO> toDomainDTOs(Iterable<AccountRoleRequestDTO> requestDTOs);

    @Override
    List<AccountRoleResponseDTO> toResponseDTOs(Iterable<AccountRoleDTO> domainDTOs);
}