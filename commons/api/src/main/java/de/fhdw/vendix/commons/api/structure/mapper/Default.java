package de.fhdw.vendix.commons.api.structure.mapper;

import java.lang.annotation.*;

/**
 * Annotation that is consumed by {@code MapStruct} and its automatic method generation.
 *
 * <p> This annotation ensures that the constructor, the one that is marked, should be used.
 *
 * <p>See also:
 * <a href="https://mapstruct.org/documentation/stable/reference/html/#third-party-api-integration">
 *      MapStruct Third-party API integration
 * </a>
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.CLASS)
public @interface Default {}