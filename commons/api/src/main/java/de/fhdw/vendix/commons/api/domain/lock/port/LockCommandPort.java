package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.web.LockCommandApi;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

public interface LockCommandPort extends CommandPort, LockCommandApi {}