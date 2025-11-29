package de.fhdw.fillialensystem.utility;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class InstanceProvider {

    private final String instanceId = UUID.randomUUID().toString();

    public String getInstanceId() {
        return instanceId;
    }
}
