package de.fhdw.vendix.commons.spring.core.mapper.api;

import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = SpringMapperConfig.class)
public interface AccountApiMapper {}