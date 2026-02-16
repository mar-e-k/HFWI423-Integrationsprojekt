package de.fhdw.vendix.store.persistence.service;

import de.fhdw.vendix.store.persistence.entity.Register;
import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.repository.RegisterRepository;
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