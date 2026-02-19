package de.fhdw.vendix.commons.api.account;

import de.fhdw.vendix.commons.api.marker.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.account.dto.AccountRequestDTO;
import de.fhdw.vendix.commons.api.account.dto.AccountResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface AccountDTOMapper extends GenericDTOMapper<AccountDTO, AccountRequestDTO, AccountResponseDTO> {}