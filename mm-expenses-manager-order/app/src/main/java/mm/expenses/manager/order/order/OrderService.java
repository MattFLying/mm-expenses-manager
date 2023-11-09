package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.order.exception.OrderNotFoundException;
import mm.expenses.manager.order.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderCreator orderCreator;
    private final OrderUpdater orderUpdater;

    Optional<Order> findById(final String id) {
        log.info("Finding order of id: {}", id);
        final var found = repository.findById(id);
        if (found.isEmpty()) {
            log.info("Order of id: {} not found", id);
        } else {
            log.info("Order of id: {} found", id);
        }
        return found.map(mapper::map);
    }

    Page<Order> findAll(final Pageable pageable) {
        log.info("Finding all orders with pageable settings: {}", pageable);
        final var result = repository.findAll(pageable);
        log.info("Found: {} orders", result.getTotalElements());
        return new PageImpl<>(result.stream().map(mapper::map).collect(Collectors.toList()), result.getPageable(), result.getTotalElements());
    }

    Optional<Order> create(final CreateNewOrderRequest newOrder) {
        return orderCreator.create(newOrder);
    }

    Optional<Order> update(final String id, final UpdateOrderRequest updateProduct) {
        final var existedObject = repository.findById(id);
        if (existedObject.isEmpty()) {
            log.error("Order of id: {} does not exists and cannot be updated", id);
            throw new OrderNotFoundException("Object of id: " + id + " does not exists and cannot be updated.");
        }
        return orderUpdater.update(existedObject.get(), updateProduct);
    }

    void remove(final String id) {
        log.info("Deleting order of id: {}", id);
        if (!objectExists(id)) {
            log.info("Order of id: {} does not exists and cannot be removed", id);
            throw new OrderNotFoundException("Object of id: " + id + " does not exists and cannot be removed");
        }
        repository.deleteById(id);
        log.info("Order of id: {} has been removed", id);
    }

    void removeByIds(final Set<String> ids) {
        log.info("Deleting {} orders of ids: {}", ids.size(), ids);
        final long removedCount = repository.deleteByIdIn(ids);
        log.info("{} orders were removed", removedCount);
    }

    private boolean objectExists(final String id) {
        return repository.existsById(id);
    }

}
