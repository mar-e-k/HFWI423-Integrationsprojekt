package de.fhdw.vendix.store.core.persistance.register;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.persistance.register.port.RegisterService;
import org.springframework.stereotype.Service;

@Service
class RegisterAdapter extends AbstractDtoCrudAdapter<Register, RegisterDTO, Long> implements RegisterService {

    private final RegisterEntityAdapter registerEntityAdapter;
    private final RegisterMapper registerMapper;

    RegisterAdapter(RegisterEntityAdapter registerEntityAdapter, RegisterMapper registerMapper) {
        super(registerEntityAdapter, registerMapper);
        this.registerEntityAdapter = registerEntityAdapter;
        this.registerMapper = registerMapper;
    }
}