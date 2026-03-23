package de.fhdw.vendix.commons.spring.data.crud;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.api.structure.entity.Identifiable;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class AbstractDtoCrudAdapter<ENT extends Identifiable<ID>, DTO extends Record & DomainDTO<ID>, ID> implements CrudQueryPort<DTO, ID>, CrudCommandPort<DTO, ID> {

    private final CrudQueryPort<ENT, ID> queryPort;
    private final CrudCommandPort<ENT, ID> commandPort;
    private final EntityMapper<ENT, DTO> mapper;

    protected AbstractDtoCrudAdapter(CrudQueryPort<ENT, ID> queryPort, CrudCommandPort<ENT, ID> commandPort, EntityMapper<ENT, DTO> mapper) {
        this.queryPort = queryPort;
        this.commandPort = commandPort;
        this.mapper = mapper;
    }

    protected <P extends CrudQueryPort<ENT, ID> & CrudCommandPort<ENT, ID>> AbstractDtoCrudAdapter(P adapter, EntityMapper<ENT, DTO> mapper) {
        this.queryPort = adapter;
        this.commandPort = adapter;
        this.mapper = mapper;
    }

    @Override
    public DTO create(DTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        return applyEntityOperation(dto, commandPort::create);
    }

    @Override
    public List<DTO> createAll(Iterable<DTO> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        return applyEntityOperation(dtos, commandPort::createAll);
    }

    @Override
    public DTO update(DTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        return applyEntityOperation(dto, commandPort::update);
    }

    @Override
    public List<DTO> updateAll(Iterable<DTO> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        return applyEntityOperation(dtos, commandPort::updateAll);
    }

    @Override
    public void delete(DTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        ENT entity = mapper.toEntity(dto);
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
    public void deleteAll(Iterable<DTO> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        List<ENT> bases = mapper.toEntities(dtos);
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
    public boolean exists(DTO dto) {
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
    public boolean existsAll(Iterable<DTO> dtos) {
        if (dtos == null) {
            return false;
        }

        List<ENT> bases = mapper.toEntities(dtos);
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
    public Optional<DTO> find(DTO dto) {
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
    public Optional<DTO> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }

        return queryPort.findById(id).map(mapper::toDTO);
    }

    @Override
    public List<DTO> findAll() {
        return mapper.toDTOs(queryPort.findAll());
    }

    @Override
    public List<DTO> findAll(Iterable<DTO> dtos) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        List<ENT> entities = mapper.toEntities(dtos);
        return mapper.toDTOs(queryPort.findAll(entities));
    }

    @Override
    public List<DTO> findAllById(Iterable<ID> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("Parameter 'ids' cannot be null");
        }
        return mapper.toDTOs(queryPort.findAllById(ids));
    }

    @Override
    public long count() {
        return queryPort.count();
    }


    /**
     * Performs an operation with the dto where the dto has to be converted to an entity beforehand.
     * A usual transformation flow of the dto is: DTO --[map]--> Entity --[operation]--> Entity --[map]--> DTO
     * @param dto to be converted to an entity
     * @param operation to be used on the converted dto
     * @return the transformed dto, after an operation has been done with it
     */
    private DTO applyEntityOperation(DTO dto, UnaryOperator<ENT> operation) {
        if (dto == null) {
            throw new IllegalArgumentException("Parameter 'dto' cannot be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Parameter 'operation' cannot be null");
        }

        ENT entity = mapper.toEntity(dto);
        ENT result = operation.apply(entity);
        return mapper.toDTO(result);
    }

    /**
     * Performs an operation with the list of dtos where the dto has to be converted to an entity beforehand.
     * A usual transformation flow of the dto is: DTO --[map]--> Entity --[operation]--> Entity --[map]--> DTO
     * @param dtos to be converted to an entity
     * @param operation to be used on the converted dto
     * @return the transformed dtos, after an operation has been done with them
     */
    private List<DTO> applyEntityOperation(Iterable<DTO> dtos, Function<Iterable<ENT>, Iterable<ENT>> operation) {
        if (dtos == null) {
            throw new IllegalArgumentException("Parameter 'dtos' cannot be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Parameter 'operation' cannot be null");
        }

        List<ENT> entities = mapper.toEntities(dtos);
        Iterable<ENT> result = operation.apply(entities);
        return mapper.toDTOs(result);
    }
}