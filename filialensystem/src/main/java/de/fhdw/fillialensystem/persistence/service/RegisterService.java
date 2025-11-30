package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.repository.RegisterRepository;
import org.springframework.stereotype.Service;

@Service
public class RegisterService extends AbstractCrudService<Register, Long> {

    public RegisterService(RegisterRepository registerRepository) {
        super(registerRepository);
    }
}