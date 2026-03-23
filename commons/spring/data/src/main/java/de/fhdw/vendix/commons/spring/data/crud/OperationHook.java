package de.fhdw.vendix.commons.spring.data.crud;

import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

public abstract class OperationHook<ENT extends Identifiable<?>> {

    private static final Logger log = LoggerFactory.getLogger(OperationHook.class);

    private final Class<?> entityClass;

    protected OperationHook() {
        Class<?> targetClass = AopUtils.getTargetClass(this);

        ResolvableType type = ResolvableType
                .forClass(targetClass)
                .as(OperationHook.class);

        this.entityClass = type.getGeneric(0).resolve(Object.class);
    }

    protected void beforeCreate(ENT entity) {
        log.atInfo().log("[CREATE] Creating {} '{}'", entityClass.getSimpleName(), entity);
    }

    protected void afterCreate(ENT entity) {
        log.atInfo().log("[CREATE] Successfully created {} '{}'", entityClass.getSimpleName(), entity);
    }

    protected void beforeUpdate(ENT entity) {
        log.atInfo().log("[UPDATE] Updating {} '{}'", entityClass.getSimpleName(), entity);
    }

    protected void afterUpdate(ENT entity) {
        log.atInfo().log("[UPDATE] Successfully updated {} '{}'", entityClass.getSimpleName(), entity);
    }

    protected void beforeDelete(ENT entity) {
        log.atInfo().log("[DELETE] Deleting {} '{}'", entityClass.getSimpleName(), entity);
    }

    protected void afterDelete(ENT entity) {
        log.atInfo().log("[DELETE] Successfully deleted {} '{}'", entityClass.getSimpleName(), entity);
    }
}