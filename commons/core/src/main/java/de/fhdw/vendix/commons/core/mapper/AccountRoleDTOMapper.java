package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleRequestDTO;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountRoleDTOMapper extends DTOMapper<AccountRoleDTO, AccountRoleRequestDTO, AccountRoleResponseDTO> {

    AccountRoleDTOMapper INSTANCE = Mappers.getMapper(AccountRoleDTOMapper.class);

    @Override
    AccountRoleDTO toDTO(AccountRoleRequestDTO dto);

    @Override
    AccountRoleResponseDTO toResponseDTO(AccountRoleDTO accountRoleDTO);
}