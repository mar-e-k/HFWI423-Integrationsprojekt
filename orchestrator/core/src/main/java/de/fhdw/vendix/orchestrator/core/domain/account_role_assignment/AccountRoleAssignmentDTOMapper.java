package de.fhdw.vendix.orchestrator.core.domain.account_role_assignment;

import de.fhdw.vendix.commons.api.domain.account_role_assignment.AccountRoleAssignmentDTO;
import de.fhdw.vendix.commons.api.domain.account_role_assignment.AccountRoleAssignmentRequestDTO;
import de.fhdw.vendix.commons.api.domain.account_role_assignment.AccountRoleAssignmentResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface AccountRoleAssignmentDTOMapper extends DTOMapper<AccountRoleAssignmentDTO, AccountRoleAssignmentRequestDTO, AccountRoleAssignmentResponseDTO> {

    @Override
    AccountRoleAssignmentDTO toDomainDTO(AccountRoleAssignmentRequestDTO requestDTO);

    @Override
    AccountRoleAssignmentResponseDTO toResponseDTO(AccountRoleAssignmentDTO domainDTO);

    @Override
    Set<AccountRoleAssignmentDTO> toDomainDTOs(Iterable<AccountRoleAssignmentRequestDTO> requestDTOs);

    @Override
    Set<AccountRoleAssignmentResponseDTO> toResponseDTOs(Iterable<AccountRoleAssignmentDTO> domainDTOs);
}