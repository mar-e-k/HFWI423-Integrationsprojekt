package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountRequestDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import org.mapstruct.Mapper;

@Mapper
public interface AccountDTOMapper extends GenericDTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {}