package de.fhdw.vendix.commons.spring.vaadin.layout;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import de.fhdw.vendix.commons.spring.data.service.CrudService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public abstract class AbstractLazySortableGrid<T> extends Grid<T> {

    private final CrudService<T, ?> service;

    protected AbstractLazySortableGrid(Class<T> clazz, CrudService<T, ?> service) {
        super(clazz, false);

        this.service = service;

        setSizeFull();

        configureColumns();
        configureDataProvider();
    }

    protected abstract void configureColumns();

    private void configureDataProvider() {
        setItems(
                query -> {
                    int page = query.getOffset() / query.getLimit();
                    int limit = query.getLimit();

                    Sort sort = mapSort(query);

                    return service
                            .findAll(PageRequest.of(page, limit, sort))
                            .stream();
                },
                query -> (int) service.count()
        );
    }

    protected Sort mapSort(Query<T, Void> query) {
        if (query.getSortOrders().isEmpty()) {
            return Sort.unsorted();
        }

        return Sort.by(
                query.getSortOrders().stream()
                        .map(order -> {
                            String property = order.getSorted();

                            return new Sort.Order(
                                    order.getDirection() == SortDirection.ASCENDING
                                            ? Sort.Direction.ASC
                                            : Sort.Direction.DESC,
                                    property
                            );
                        })
                        .toList()
        );
    }
}