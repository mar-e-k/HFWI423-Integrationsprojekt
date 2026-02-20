package de.fhdw.vendix.commons.api.domain.account;

import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountRequestDTO;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface AccountDTOMapper extends GenericDTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {}