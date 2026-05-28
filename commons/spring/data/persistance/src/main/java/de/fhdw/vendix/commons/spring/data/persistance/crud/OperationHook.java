package de.fhdw.vendix.commons.spring.data.persistance.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OperationHook {

    private static final Logger log = LoggerFactory.getLogger(OperationHook.class);

    protected OperationHook() {}

    protected <T> void beforeCreate(T entity) {
        log.atInfo().log("[CREATE] Creating {} '{}'", entity.getClass().getSimpleName(), entity);
    }

    protected <T> void afterCreate(T entity) {
        log.atInfo().log("[CREATE] Successfully created {} '{}'", entity.getClass().getSimpleName(), entity);
    }

    protected <T> void beforeUpdate(T entity) {
        log.atInfo().log("[UPDATE] Updating {} '{}'", entity.getClass().getSimpleName(), entity);
    }

    protected <T> void afterUpdate(T entity) {
        log.atInfo().log("[UPDATE] Successfully updated {} '{}'", entity.getClass().getSimpleName(), entity);
    }

    protected <T> void beforeDelete(T entity) {
        log.atInfo().log("[DELETE] Deleting {} '{}'", entity.getClass().getSimpleName(), entity);
    }

    protected <T> void afterDelete(T entity) {
        log.atInfo().log("[DELETE] Successfully deleted {} '{}'", entity.getClass().getSimpleName(), entity);
    }
}