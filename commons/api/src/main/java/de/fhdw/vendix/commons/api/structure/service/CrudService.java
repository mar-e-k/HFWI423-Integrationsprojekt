package de.fhdw.vendix.commons.api.structure.service;

public interface CrudService<T, ID> extends CrudQueryService<T, ID>, CrudCommandService<T, ID>{}