package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.RegisterDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.store.persistence.entity.Register;
import de.fhdw.vendix.store.persistence.entity.Store;
import org.springframework.stereotype.Component;

@Component
public class RegisterMapper implements GenericMapper<Register, RegisterDTO> {

    public RegisterMapper() {
        super();
    }

    @Override
    public Register toEntity(RegisterDTO dto) {
        Register register = new Register();
        register.setId(dto.getId());
        register.setStore(new Store(dto.getStore()));
        return register;
    }

    @Override
    public RegisterDTO toDto(Register register) {
        RegisterDTO dto = new RegisterDTO();
        dto.setId(register.getId());
        dto.setStore(register.getStore().getId());
        return dto;
    }
}