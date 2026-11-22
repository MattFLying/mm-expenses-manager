package mm.expenses.manager.order.core;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import mm.expenses.manager.order.api.order.OrderApi;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.processor.search.OrderFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.UUID;

import static mm.expenses.manager.common.web.api.WebApi.ID_URL;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = OrderController.PUBLIC_API_URL)
class OrderController implements OrderApi {

    static final String PUBLIC_API_URL = "/api/v1";

    private final OrderService service;
    private final PaginationHelper pagination;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @PreAuthorize("hasAnyAuthority('getOwnOrdersInfo', 'getOrdersInfo')")
    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER, required = false) final Integer pageNumber,
                                             @RequestParam(value = PaginationConfig.PAGE_SIZE, required = false) final Integer pageSize,
                                             @RequestParam(value = PaginationConfig.SORT, required = false) final SortOrderRequest sortOrder,

                                             @RequestParam(value = OrderFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted,
                                             @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency,
                                             @RequestParam(value = OrderFilter.SHOULD_SEARCH_ALL_ORDERS_PROPERTY, required = false) final Boolean shouldSearchAllOrders,

                                             @RequestParam(value = OrderFilter.NAME_PROPERTY, required = false) final String name,
                                             @RequestParam(value = OrderFilter.NAME_OPERATION_PROPERTY, required = false) final TextOperationRequest nameOperation,

                                             @RequestParam(value = OrderFilter.PRODUCTS_COUNT_PROPERTY, required = false) final Integer productsCount,
                                             @RequestParam(value = OrderFilter.PRODUCTS_COUNT_OPERATION_PROPERTY, required = false) final NumberOperationRequest productsCountOperation,

                                             @RequestParam(value = OrderFilter.GENERAL_QUERY_PROPERTY, required = false) final String query) {

        final var securityContext = currentUserProvider.getSecurityContext();
        final var queryFilter = OrderFilter.builder()
                .sortConfig(OrderSortOrder.of(sortOrder))
                .name(name)
                .nameOperation(nameOperation)
                .productsCount(productsCount)
                .productsCountOperation(productsCountOperation)
                .query(query)
                .isDeleted(isDeleted)
                .shouldConvertCurrency(shouldConvertCurrency)
                .shouldSearchAllOrders(shouldSearchAllOrders)
                .paginationConfig(pagination.getPageRequest(pageNumber, pageSize))
                .securityContext(securityContext)
                .build();
        return ResponseEntity.ok(service.search(securityContext, queryFilter, queryFilter.shouldConvertCurrenciesToDefault()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('getOwnOrderInfo', 'getOrderInfo')")
    @GetMapping(value = "/orders/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> findById(@PathVariable("id") final UUID id,
                                                  @RequestParam(value = OrderFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted,
                                                  @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency) {
        return ResponseEntity.ok(service.findById(currentUserProvider.getSecurityContext(), id, isDeleted, shouldConvertCurrency));
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('createOwnOrder', 'createOrder')")
    @PostMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> create(@RequestBody final CreateNewOrderRequest request,
                                                @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(currentUserProvider.getSecurityContext(), request, shouldConvertCurrency));
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('updateOwnOrder', 'updateOrder')")
    @PatchMapping(value = "/orders" + ID_URL, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> update(@PathVariable("id") final UUID id,
                                                @RequestBody final UpdateOrderRequest request,
                                                @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency) {
        return ResponseEntity.ok(service.update(currentUserProvider.getSecurityContext(), id, request, shouldConvertCurrency));
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('deleteOwnOrder', 'deleteOrder')")
    @DeleteMapping(value = "/orders" + ID_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("id") final UUID id) {
        service.delete(currentUserProvider.getSecurityContext(), id);

        return ResponseEntity.noContent().build();
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('deleteOwnOrder', 'deleteOrder')")
    @DeleteMapping(value = "/orders/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByIds(@RequestBody final OrderIds request) {
        service.delete(currentUserProvider.getSecurityContext(), new HashSet<>(request.getIds()));

        return ResponseEntity.noContent().build();
    }

}
