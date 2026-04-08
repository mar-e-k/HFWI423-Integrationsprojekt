package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class RegisterServiceImpl extends AbstractEntityCrudAdapter<Register, Long> implements RegisterService {

    private final RegisterRepository registerRepository;

    RegisterServiceImpl(RegisterRepository registerRepository) {
        super(registerRepository);
        this.registerRepository = registerRepository;
    }
}