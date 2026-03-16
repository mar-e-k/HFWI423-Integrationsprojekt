package de.fhdw.vendix.commons.spring.core.crud;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class AbstractMappedCrudAdapter<T extends Identifiable<ID>, ID, D extends Record & DomainDTO<ID>>
        implements CrudQueryPort<D, ID>, CrudCommandPort<D, ID> {

    private final CrudQueryPort<T, ID> queryPort;
    private final CrudCommandPort<T, ID> commandPort;
    private final EntityMapper<ID, T, D> mapper;

    protected AbstractMappedCrudAdapter(CrudQueryPort<T, ID> queryPort, CrudCommandPort<T, ID> commandPort, EntityMapper<ID, T, D> mapper) {
        if (queryPort == null) {
            throw new IllegalArgumentException("Parameter 'queryPort' cannot be null");
        }
        if (commandPort == null) {
            throw new IllegalArgumentException("Parameter 'commandPort' cannot be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Parameter 'mapper' cannot be null");
        }
        this.queryPort = queryPort;
        this.commandPort = commandPort;
        this.mapper = mapper;
    }

    @Override
    public D create(D dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        return applyEntityOperation(dto, commandPort::create);
    }

    @Override
    public List<D> createAll(Iterable<D> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        return applyEntityOperation(dtos, commandPort::createAll);
    }

    @Override
    public D update(D dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        return applyEntityOperation(dto, commandPort::update);
    }

    @Override
    public List<D> updateAll(Iterable<D> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        return applyEntityOperation(dtos, commandPort::updateAll);
    }

    @Override
    public void delete(D dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        T entity = mapper.toEntity(dto);
        commandPort.delete(entity);
    }

    @Override
    public void deleteById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' cannot be null");
        }
        commandPort.deleteById(id);
    }

    @Override
    public void deleteAll() {
        commandPort.deleteAll();
    }

    @Override
    public void deleteAll(Iterable<D> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        List<T> bases = mapper.toEntities(dtos);
        commandPort.deleteAll(bases);
    }

    @Override
    public void deleteAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }
        commandPort.deleteAllById(ids);
    }

    @Override
    public boolean exists(D dto) {
        if (dto == null) {
            return false;
        }

        ID id = dto.getIdentifiable();
        if (id == null) {
            return false;
        }

        return queryPort.existsById(id);
    }

    @Override
    public boolean existsById(ID id) {
        if (id == null) {
            return false;
        }

        return queryPort.existsById(id);
    }

    @Override
    public boolean existsAll(Iterable<D> dtos) {
        if (dtos == null) {
            return false;
        }

        List<T> bases = mapper.toEntities(dtos);
        return queryPort.existsAll(bases);
    }

    @Override
    public boolean existsAllById(Iterable<ID> ids) {
        if (ids == null) {
            return false;
        }

        return queryPort.existsAllById(ids);
    }

    @Override
    public Optional<D> find(D dto) {
        if (dto == null) {
            return Optional.empty();
        }

        ID id = dto.getIdentifiable();
        if (id == null) {
            return Optional.empty();
        }

        return queryPort.findById(id).map(mapper::toDTO);
    }

    @Override
    public Optional<D> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }

        return queryPort.findById(id).map(mapper::toDTO);
    }

    @Override
    public List<D> findAll() {
        return mapper.toDTOs(queryPort.findAll());
    }

    @Override
    public List<D> findAll(Iterable<D> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        List<T> entities = mapper.toEntities(dtos);
        return mapper.toDTOs(queryPort.findAll(entities));
    }

    @Override
    public List<D> findAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }
        return mapper.toDTOs(queryPort.findAllById(ids));
    }

    @Override
    public long count() {
        return queryPort.count();
    }

    private D applyEntityOperation(D dto, UnaryOperator<T> operation) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Parameter 'operation' cannot be null");
        }

        T entity = mapper.toEntity(dto);
        T result = operation.apply(entity);
        return mapper.toDTO(result);
    }

    private List<D> applyEntityOperation(Iterable<D> dtos, Function<Iterable<T>, Iterable<T>> operation) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Parameter 'operation' cannot be null");
        }

        List<T> entities = mapper.toEntities(dtos);
        Iterable<T> result = operation.apply(entities);
        return mapper.toDTOs(result);
    }
}