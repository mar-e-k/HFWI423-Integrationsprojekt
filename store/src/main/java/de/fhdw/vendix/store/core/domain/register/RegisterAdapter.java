package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class RegisterAdapter extends AbstractSpringDataCrudLogAdapter<Register, Long> {

    private final RegisterRepository registerRepository;
    private final RegisterMapper registerMapper;

    public RegisterAdapter(RegisterRepository registerRepository, RegisterMapper registerMapper) {
        super(registerRepository);
        this.registerRepository = registerRepository;
        this.registerMapper = registerMapper;
    }
}