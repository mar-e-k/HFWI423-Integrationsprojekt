package de.fhdw.vendix.orchestrator.core.domain.account_role;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleRequestDTO;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleResponseDTO;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface AccountRoleDTOMapper extends DTOMapper<AccountRoleDTO, AccountRoleRequestDTO, AccountRoleResponseDTO> {

    @Override
    AccountRoleDTO toDomainDTO(AccountRoleRequestDTO dto);

    @Override
    AccountRoleResponseDTO toResponseDTO(AccountRoleDTO accountRoleDTO);

    @Override
    Set<AccountRoleDTO> toDomainDTOs(Iterable<AccountRoleRequestDTO> requestDTOs);

    @Override
    Set<AccountRoleResponseDTO> toResponseDTOs(Iterable<AccountRoleDTO> domainDTOs);
}