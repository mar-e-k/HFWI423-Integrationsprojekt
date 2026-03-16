package de.fhdw.vendix.store.core.persistance.register;

import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class RegisterAdapter extends AbstractCrudLogAdapter<Register, Long> {

    private final RegisterRepository registerRepository;
    private final RegisterMapper registerMapper;

    public RegisterAdapter(RegisterRepository registerRepository, RegisterMapper registerMapper) {
        super(registerRepository);
        this.registerRepository = registerRepository;
        this.registerMapper = registerMapper;
    }
}