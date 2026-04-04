package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class RegisterEntityAdapter extends AbstractEntityCrudAdapter<Register, Long> {

    private final RegisterRepository registerRepository;

    RegisterEntityAdapter(RegisterRepository registerRepository) {
        super(registerRepository);
        this.registerRepository = registerRepository;
    }
}