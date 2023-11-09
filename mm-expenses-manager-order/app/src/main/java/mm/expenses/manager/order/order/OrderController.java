package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.exception.api.ApiNotFoundException;
import mm.expenses.manager.order.api.order.OrderApi;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.order.exception.OrderCreationException;
import mm.expenses.manager.order.order.exception.OrderNotFoundException;
import mm.expenses.manager.order.order.exception.OrderUpdateException;
import mm.expenses.manager.order.pageable.PageFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

import static mm.expenses.manager.order.config.UrlDefaultPaths.ORDER_URL;

@RestController
@RequiredArgsConstructor
@RequestMapping(ORDER_URL)
class OrderController implements OrderApi {

    private final OrderService service;
    private final OrderMapper mapper;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse create(@RequestBody CreateNewOrderRequest request) {
        try {
            return service.create(mapper.mapToNewRequest(request))
                    .map(mapper::mapToResponse)
                    .orElseThrow();
        } catch (final OrderCreationException exception) {
            throw new ApiBadRequestException(null);
        }
    }

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderPage findAll(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                             @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return mapper.mapToPageResponse(
                service.findAll(
                        PageFactory.getPageRequest(pageNumber, pageSize)
                )
        );
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse findById(@PathVariable("id") String id) {
        return service.findById(id)
                .map(mapper::mapToResponse)
                .orElseThrow();
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse update(@PathVariable("id") String id, @RequestBody UpdateOrderRequest request) {
        try {
            return service.update(id, mapper.mapToUpdatedRequest(request))
                    .map(mapper::mapToResponse)
                    .orElseThrow();
        } catch (final OrderNotFoundException exception) {
            throw new ApiNotFoundException(null);
        } catch (final OrderUpdateException exception) {
            throw new ApiBadRequestException(null);
        }
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteById(@PathVariable("id") String id) {
        try {
            service.remove(id);
        } catch (final OrderNotFoundException exception) {
            throw new ApiNotFoundException(null);
        }
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteByIds(@RequestBody OrderIds request) {
        try {
            service.removeByIds(new HashSet<>(request.getIds()));
        } catch (final OrderNotFoundException exception) {
            throw new ApiNotFoundException(null);
        }
    }

    // To be tracked later
    /*
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

    @GetMapping(value = "/{id}")
    ResponseEntity<OrderDto> findById(@PathVariable("id") final String id) {
        return service.findById(id)
                .map(mapper::mapToDto)
                .map(product -> ResponseEntity.ok().body(product))
                .orElseThrow();
                //.orElseThrow(() -> new ApiNotFoundException(OrderExceptionCode.NOT_FOUND.getCode(), "Order of id: " + id + " does not exists."));
    }

    @GetMapping
    Page<OrderDto> findAll(final Pageable pageable) {
        final var result = service.findAll(pageable).stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(result, pageable, result.size());
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
    }*/

}
