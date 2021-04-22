package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.exception.api.ApiNotFoundException;
import mm.expenses.manager.order.order.exception.OrderCreationException;
import mm.expenses.manager.order.order.exception.OrderExceptionCode;
import mm.expenses.manager.order.order.exception.OrderNotFoundException;
import mm.expenses.manager.order.order.exception.OrderUpdateException;
import mm.expenses.manager.order.order.model.CreateNewOrder;
import mm.expenses.manager.order.order.model.OrderDto;
import mm.expenses.manager.order.order.model.UpdateOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

import static mm.expenses.manager.order.UrlDefaultPaths.ORDER_URL;

@RestController
@RequestMapping(ORDER_URL)
@RequiredArgsConstructor
class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    @GetMapping
    Page<OrderDto> findAll(final Pageable pageable) {
        final var result = service.findAll(pageable).stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(result, pageable, result.size());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<OrderDto> findById(@PathVariable("id") final String id) {
        return service.findById(id)
                .map(mapper::mapToDto)
                .map(product -> ResponseEntity.ok().body(product))
                .orElseThrow();
                //.orElseThrow(() -> new ApiNotFoundException(OrderExceptionCode.NOT_FOUND.getCode(), "Order of id: " + id + " does not exists."));
    }

    @PostMapping
    ResponseEntity<OrderDto> create(@RequestBody final CreateNewOrder newProduct) {
        try {
            return service.create(newProduct)
                    .map(mapper::mapToDto)
                    .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                    .orElseThrow();
                    //.orElseThrow(() -> new ApiNotFoundException(OrderExceptionCode.NOT_FOUND.getCode(), "Something went wrong with creating the new order"));
        } catch (final OrderCreationException exception) {
            //throw new ApiBadRequestException(OrderExceptionCode.NEW_ORDER_VALIDATION.getCode(), exception.getMessage());
            throw new ApiBadRequestException(null);
        }
    }

    @PutMapping(value = "/{id}")
    ResponseEntity<OrderDto> update(@PathVariable("id") final String id, @RequestBody final UpdateOrder product) {
        try {
            return service.update(id, product)
                    .map(mapper::mapToDto)
                    .map(updated -> ResponseEntity.status(HttpStatus.CREATED).body(updated))
                    .orElseThrow();
                    //.orElseThrow(() -> new ApiNotFoundException(OrderExceptionCode.NOT_FOUND.getCode(), "Something went wrong with updating the order"));
        } catch (final OrderNotFoundException exception) {
            throw notFoundException(exception);
        } catch (final OrderUpdateException exception) {
            //throw new ApiBadRequestException(OrderExceptionCode.UPDATE_ORDER_VALIDATION.getCode(), exception.getMessage());
            throw new ApiBadRequestException(null);
        }
    }

    @DeleteMapping(value = "/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") final String id) {
        try {
            service.remove(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (final OrderNotFoundException exception) {
            throw notFoundException(exception);
        }
    }

    @PostMapping(value = "/remove")
    ResponseEntity<Void> delete(@RequestBody final Set<String> ids) {
        try {
            service.removeByIds(ids);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (final OrderNotFoundException exception) {
            throw notFoundException(exception);
        }
    }

    private ApiNotFoundException notFoundException(final OrderNotFoundException exception) {
        //return new ApiNotFoundException(OrderExceptionCode.NOT_FOUND.getCode(), exception.getMessage());
        return new ApiNotFoundException(null);
    }

}
