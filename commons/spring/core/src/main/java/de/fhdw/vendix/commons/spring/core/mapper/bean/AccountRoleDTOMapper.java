package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleRequestDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.DTOMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = DTOMapperConfig.class)
public interface AccountRoleDTOMapper extends DTOMapper<AccountRoleDTO, AccountRoleRequestDTO, AccountRoleResponseDTO> {

    @Override
    AccountRoleDTO toDomainDTO(AccountRoleRequestDTO dto);

    @Override
    AccountRoleResponseDTO toResponseDTO(AccountRoleDTO accountRoleDTO);
}