package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.web.RequestProcessor;
import mm.expenses.manager.common.web.WebInterceptor;
import mm.expenses.manager.common.web.WebContext;
import mm.expenses.manager.common.web.exception.ApiBadRequestException;
import mm.expenses.manager.common.web.exception.ApiConflictException;
import mm.expenses.manager.order.api.order.OrderApi;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import static mm.expenses.manager.common.web.api.WebApi.ID_URL;

@RestController
@RequiredArgsConstructor
@RequestMapping(OrderWebApi.BASE_URL)
class OrderController implements OrderApi {

    private final PaginationHelper pagination;
    private final WebInterceptor interceptor;

    private final OrderMapper mapper;
    private final OrderService service;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER_PROPERTY, required = false) final Integer pageNumber,
                                             @RequestParam(value = PaginationConfig.PAGE_SIZE_PROPERTY, required = false) final Integer pageSize,
                                             @RequestParam(value = PaginationConfig.SORT_ORDER_PROPERTY, required = false) final SortOrderRequest sortOrder,
                                             @RequestParam(value = PaginationConfig.SORT_DESC_PROPERTY, required = false) final Boolean sortDesc,
                                             @RequestParam(value = OrderQueryFilter.NAME_PROPERTY, required = false) final String name,
                                             @RequestParam(value = OrderQueryFilter.PRICE_SUMMARY_PROPERTY, required = false) final BigDecimal priceSummary,
                                             @RequestParam(value = OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, required = false) final Boolean priceSummaryLessThan,
                                             @RequestParam(value = OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, required = false) final Boolean priceSummaryGreaterThan,
                                             @RequestParam(value = OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, required = false) final Integer productsCount,
                                             @RequestParam(value = OrderQueryFilter.PRODUCTS_COUNT_LESS_THAN_PROPERTY, required = false) final Boolean productsCountLessThan,
                                             @RequestParam(value = OrderQueryFilter.PRODUCTS_COUNT_GREATER_THAN_PROPERTY, required = false) final Boolean productsCountGreaterThan) {
        final var context = WebContext.of(OrderWebApi.FIND_ALL);
        final RequestProcessor processor = webContext -> {
            if (pagination.isPageNumberAndPageSizePresent(pageNumber, pageSize)) {
                throw new ApiBadRequestException(OrderExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED);
            }

            final var queryFilter = new OrderQueryFilter(name, priceSummary, productsCount, priceSummaryLessThan, priceSummaryGreaterThan, productsCountLessThan, productsCountGreaterThan);
            if (queryFilter.isPriceSummaryOriented()) {
                if (queryFilter.isPriceSummaryLessAndGreaterUsed()) {
                    throw new ApiBadRequestException(OrderExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE);
                }
                if (!queryFilter.isPriceSummaryLessOrGreaterOriented()) {
                    throw new ApiBadRequestException(OrderExceptionMessage.PRICE_MUST_BE_LESS_THAN_OR_GREATER_THAN);
                }
            }
            if (queryFilter.isProductsCountOriented()) {
                if (queryFilter.isProductsCountLessAndGreaterUsed()) {
                    throw new ApiBadRequestException(OrderExceptionMessage.PRODUCTS_COUNT_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE);
                }
            }
            return mapper.mapToPageResponse(service.findOrders(queryFilter, pagination.getPageRequest(pageNumber, pageSize), OrderSortOrder.of(sortOrder, sortDesc)));
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> findById(@PathVariable("id") final UUID id,
                                                  @RequestParam(value = OrderQueryFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted) {
        final var context = WebContext.of(OrderWebApi.FIND_BY_ID).requestId(id);
        final RequestProcessor processor = webContext -> mapper.mapToResponse(service.findById(id, isDeleted));
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> create(@RequestBody final CreateNewOrderRequest request) {
        final var context = WebContext.of(OrderWebApi.CREATE).requestBody(request);
        final RequestProcessor processor = webContext -> mapper.mapToResponse(service.create(request));
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = ID_URL, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> update(@PathVariable("id") final UUID id, @RequestBody final UpdateOrderRequest request) {
        final var context = WebContext.of(OrderWebApi.UPDATE).requestId(id).requestBody(request);
        final RequestProcessor processor = webContext -> {
            if (!isAnyUpdateOrder(request)) {
                throw new ApiConflictException(OrderExceptionMessage.ORDER_NO_UPDATE_DATA);
            }
            return mapper.mapToResponse(service.update(webContext.getRequestId(), request));
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = ID_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("id") final UUID id) {
        final var context = WebContext.of(OrderWebApi.DELETE).requestId(id);
        final RequestProcessor processor = webContext -> {
            service.delete(webContext.getRequestId());
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

    private boolean isAnyUpdateOrder(final UpdateOrderRequest request) {
        final var isNameUpdated = Objects.nonNull(request.getName());
        final var areNewProductsCreated = CollectionUtils.isNotEmpty(request.getNewProducts());
        final var areProductsUpdated = CollectionUtils.isNotEmpty(request.getOrderedProducts());
        final var areProductsToRemove = CollectionUtils.isNotEmpty(request.getRemoveProducts());

        return isNameUpdated || areNewProductsCreated || areProductsUpdated || areProductsToRemove;
    }

}
