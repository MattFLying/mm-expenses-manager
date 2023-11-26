package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.web.RequestProcessor;
import mm.expenses.manager.common.web.WebInterceptor;
import mm.expenses.manager.common.web.WebContext;
import mm.expenses.manager.common.web.exception.ApiBadRequestException;
import mm.expenses.manager.common.web.exception.ApiNotFoundException;
import mm.expenses.manager.order.api.order.OrderApi;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.order.exception.OrderCreationException;
import mm.expenses.manager.order.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.order.exception.OrderUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping(OrderWebApi.BASE_URL)
@RequiredArgsConstructor
class OrderController implements OrderApi {

    private final PaginationHelper pagination;
    private final WebInterceptor interceptor;

    private final OrderMapper mapper;
    private final OrderService service;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER_PROPERTY, required = false) final Integer pageNumber,
                                             @RequestParam(value = PaginationConfig.PAGE_SIZE_PROPERTY, required = false) final Integer pageSize) {
        final var pageable = pagination.getPageRequest(pageNumber, pageSize);

        final var context = WebContext.of(OrderWebApi.FIND_ALL);
        final RequestProcessor processor = webContext -> mapper.mapToPageResponse(service.findAll(pageable));

        return interceptor.processRequest(processor, context);
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> findById(@PathVariable("id") final String id) {
        final var context = WebContext.of(OrderWebApi.FIND_BY_ID).requestId(id);
        final RequestProcessor processor = webContext -> {
            final var orderId = webContext.getRequestId();

            return service.findById(orderId)
                    .map(mapper::mapToResponse)
                    .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(orderId)));
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> create(@RequestBody final CreateNewOrderRequest request) {
        final var context = WebContext.of(OrderWebApi.CREATE).requestBody(request);
        final RequestProcessor processor = webContext -> {
            try {
                final var newOrder = (CreateNewOrderRequest) webContext.getRequestBody();
                return service.create(newOrder).map(mapper::mapToResponse);
            } catch (final OrderCreationException exception) {
                throw new ApiBadRequestException(OrderExceptionMessage.NEW_ORDER_VALIDATION, exception);
            }
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> update(@PathVariable("id") final String id, @RequestBody final UpdateOrderRequest request) {
        final var context = WebContext.of(OrderWebApi.UPDATE).requestId(id).requestBody(request);
        final RequestProcessor processor = webContext -> {
            try {
                final var updateOrderRequest = (UpdateOrderRequest) webContext.getRequestBody();
                return service.update(webContext.getRequestId(), updateOrderRequest)
                        .map(mapper::mapToResponse)
                        .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(webContext.getRequestId())));
            } catch (final OrderUpdateException exception) {
                throw new ApiBadRequestException(OrderExceptionMessage.UPDATE_ORDER_VALIDATION, exception);
            }
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("id") final String id) {
        final var context = WebContext.of(OrderWebApi.DELETE).requestId(id);
        final RequestProcessor processor = webContext -> {
            service.remove(webContext.getRequestId());
            return true;
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByIds(@RequestBody final OrderIds request) {
        final var context = WebContext.of(OrderWebApi.DELETE_BY_IDS).requestIds(request.getIds());
        final RequestProcessor processor = webContext -> {
            service.removeByIds(new HashSet<>(webContext.getRequestIds()));
            return true;
        };
        return interceptor.processRequest(processor, context);
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
