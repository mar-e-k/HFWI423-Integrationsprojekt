package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
public class RegisterService extends AbstractSpringDataCrudLogAdapter<Register, Long> {

    private final RegisterRepository registerRepository;
    private final RegisterMapper registerMapper;

    public RegisterService(RegisterRepository registerRepository, RegisterMapper registerMapper) {
        super(registerRepository);
        this.registerRepository = registerRepository;
        this.registerMapper = registerMapper;
    }
}