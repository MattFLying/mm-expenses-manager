package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.pagination.PaginationHelper;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.order.api.order.OrderApi;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import static mm.expenses.manager.common.web.api.WebApi.ID_URL;

@RestController
@RequiredArgsConstructor
@RequestMapping(OrderWebApi.BASE_URL)
class OrderController implements OrderApi {

    private final PaginationHelper pagination;

    private final OrderMapper mapper;
    private final OrderService service;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER, required = false) final Integer pageNumber,
                                             @RequestParam(value = PaginationConfig.PAGE_SIZE, required = false) final Integer pageSize,
                                             @RequestParam(value = PaginationConfig.SORT, required = false) final SortOrderRequest sortOrder,

                                             @RequestParam(value = OrderFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted,
                                             @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency,

                                             @RequestParam(value = OrderFilter.NAME_PROPERTY, required = false) final String name,
                                             @RequestParam(value = OrderFilter.NAME_OPERATION_PROPERTY, required = false) final TextOperationRequest nameOperation,

                                             @RequestParam(value = OrderFilter.PRODUCTS_COUNT_PROPERTY, required = false) final Integer productsCount,
                                             @RequestParam(value = OrderFilter.PRODUCTS_COUNT_OPERATION_PROPERTY, required = false) final NumberOperationRequest productsCountOperation,

                                             @RequestParam(value = OrderFilter.GENERAL_QUERY_PROPERTY, required = false) final String query) {
        val queryFilter = OrderFilter.builder()
                .sortConfig(OrderSortOrder.of(sortOrder))
                .name(name)
                .nameOperation(nameOperation)
                .productsCount(productsCount)
                .productsCountOperation(productsCountOperation)
                .query(query)
                .isDeleted(isDeleted)
                .shouldConvertCurrency(shouldConvertCurrency)
                .paginationConfig(pagination.getPageRequest(pageNumber, pageSize))
                .build();

        return ResponseEntity.ok(mapper.mapToPageResponse(service.findOrders(queryFilter)));
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> findById(@PathVariable("id") final UUID id,
                                                  @RequestParam(value = OrderFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted,
                                                  @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency) {
        return ResponseEntity.ok(mapper.mapToResponse(service.findById(id, isDeleted, shouldConvertCurrency)));
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> create(@RequestBody final CreateNewOrderRequest request,
                                                @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.mapToResponse(service.create(request, shouldConvertCurrency)));
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = ID_URL, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> update(@PathVariable("id") final UUID id,
                                                @RequestBody final UpdateOrderRequest request,
                                                @RequestParam(value = OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency) {
        if (!isAnyUpdateOrder(request)) {
            throw new ApiConflictException(OrderExceptionMessage.ORDER_NO_UPDATE_DATA);
        }
        return ResponseEntity.ok(mapper.mapToResponse(service.update(id, request, shouldConvertCurrency)));
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = ID_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("id") final UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByIds(@RequestBody final OrderIds request) {
        service.removeByIds(new HashSet<>(request.getIds()));

        return ResponseEntity.noContent().build();
    }

    private boolean isAnyUpdateOrder(final UpdateOrderRequest request) {
        final var isNameUpdated = Objects.nonNull(request.getName());
        final var areNewProductsCreated = CollectionUtils.isNotEmpty(request.getNewProducts());
        final var areProductsUpdated = CollectionUtils.isNotEmpty(request.getOrderedProducts());
        final var areProductsToRemove = CollectionUtils.isNotEmpty(request.getRemoveProducts());

        return isNameUpdated || areNewProductsCreated || areProductsUpdated || areProductsToRemove;
    }

}
