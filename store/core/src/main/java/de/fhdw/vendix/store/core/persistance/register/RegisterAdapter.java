package de.fhdw.vendix.store.core.persistance.register;

import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.spring.core.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class RegisterAdapter extends AbstractDtoCrudAdapter<Register, RegisterDTO, Long> {

    private final RegisterEntityAdapter registerEntityAdapter;
    private final RegisterMapper registerMapper;

    RegisterAdapter(RegisterEntityAdapter registerEntityAdapter, RegisterMapper registerMapper) {
        super(registerEntityAdapter, registerMapper);
        this.registerEntityAdapter = registerEntityAdapter;
        this.registerMapper = registerMapper;
    }
}