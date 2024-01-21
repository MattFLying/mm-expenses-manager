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
import java.util.UUID;
import java.util.stream.Collectors;

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
        final var context = WebContext.of(OrderWebApi.FIND_BY_ID).requestId(UUID.fromString(id));
        final RequestProcessor processor = webContext -> {
            final var orderId = webContext.getRequestId();

            return service.findById(orderId.toString())
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
        final var context = WebContext.of(OrderWebApi.UPDATE).requestId(UUID.fromString(id)).requestBody(request);
        final RequestProcessor processor = webContext -> {
            try {
                final var updateOrderRequest = (UpdateOrderRequest) webContext.getRequestBody();
                return service.update(webContext.getRequestId().toString(), updateOrderRequest)
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
        final var context = WebContext.of(OrderWebApi.DELETE).requestId(UUID.fromString(id));
        final RequestProcessor processor = webContext -> {
            service.remove(webContext.getRequestId().toString());
            return true;
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByIds(@RequestBody final OrderIds request) {
        final var context = WebContext.of(OrderWebApi.DELETE_BY_IDS).requestIds(request.getIds().stream().map(UUID::fromString).collect(Collectors.toList()));
        final RequestProcessor processor = webContext -> {
            service.removeByIds(new HashSet<>(webContext.getRequestIds().stream().map(UUID::toString).collect(Collectors.toSet())));
            return true;
        };
        return interceptor.processRequest(processor, context);
    }

}
