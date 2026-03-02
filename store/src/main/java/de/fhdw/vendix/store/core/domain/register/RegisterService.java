package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.store.Store;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterService extends AbstractCrudService<Register, Long> {

    public RegisterService(RegisterRepository registerRepository) {
        super(registerRepository);
    }

    public List<Register> findAllByStore(Store store) {
        return ((RegisterRepository) repository).findAllByStore(store);
    }
}