package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.beans.pagination.sort.SortOrder;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.exception.OrderNotFoundException;
import mm.expenses.manager.order.exception.OrderValidationException;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductService productService;

    Page<Order> findOrders(final OrderQueryFilter queryFilter, final PageRequest pageable, final SortOrder sortOrder) {
        final var filter = queryFilter.findFilter();
        final var sort = sortOrder.getOrder();
        return switch (filter) {
            case NAME -> repository.findByNameAndNotDeleted(queryFilter.name(), pageable.withSort(jpaSort(sort)));
            case NAME_PRICE_LESS_THAN ->
                    repository.findByNameAndPriceSummaryLessThanAndNotDeleted(queryFilter.name(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case NAME_PRICE_GREATER_THAN ->
                    repository.findByNameAndPriceSummaryGreaterThanAndNotDeleted(queryFilter.name(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case NAME_PRODUCTS_COUNT ->
                    repository.findByNameAndProductsCountAndNotDeleted(queryFilter.name(), queryFilter.productsCount(), pageable.withSort(jpaSort(sort)));
            case NAME_PRODUCTS_COUNT_PRICE_LESS_THAN ->
                    repository.findByNameAndProductsCountAndPriceSummaryLessThanAndNotDeleted(queryFilter.name(), queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case NAME_PRODUCTS_COUNT_PRICE_GREATER_THAN ->
                    repository.findByNameAndProductsCountAndPriceSummaryGreaterThanAndNotDeleted(queryFilter.name(), queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));

            case PRODUCTS_COUNT ->
                    repository.findByProductsCountAndNotDeleted(queryFilter.productsCount(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_LESS_THAN ->
                    repository.findByProductsCountLessThanAndNotDeleted(queryFilter.productsCount(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_GREATER_THAN ->
                    repository.findByProductsCountGreaterThanAndNotDeleted(queryFilter.productsCount(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_PRICE_LESS_THAN ->
                    repository.findByProductsCountAndPriceSummaryLessThanAndNotDeleted(queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_PRICE_GREATER_THAN ->
                    repository.findByProductsCountAndPriceSummaryGreaterThanAndNotDeleted(queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_LESS_THAN_PRICE_LESS_THAN ->
                    repository.findByProductsCountLessThanAndPriceSummaryLessThanAndNotDeleted(queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_GREATER_THAN_PRICE_LESS_THAN ->
                    repository.findByProductsCountGreaterThanAndPriceSummaryLessThanAndNotDeleted(queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_LESS_THAN_PRICE_GREATER_THAN ->
                    repository.findByProductsCountLessThanAndPriceSummaryGreaterThanAndNotDeleted(queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case PRODUCTS_COUNT_GREATER_THAN_PRICE_GREATER_THAN ->
                    repository.findByProductsCountGreaterThanAndPriceSummaryGreaterThanAndNotDeleted(queryFilter.productsCount(), queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));

            case PRICE_LESS_THAN ->
                    repository.findByPriceSummaryLessThanAndNotDeleted(queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));
            case PRICE_GREATER_THAN ->
                    repository.findByPriceSummaryGreaterThanAndNotDeleted(queryFilter.priceSummary(), pageable.withSort(jpaSort(sort)));

            default -> repository.findAllNotDeleted(pageable.withSort(jpaSort(sort)));
        };
    }

    Order findById(final UUID id, final Boolean isDeleted) {
        final var isDeletedFlag = Objects.nonNull(isDeleted) ? isDeleted : false;
        return repository.findByIdAndIsDeleted(id, isDeletedFlag)
                .orElseThrow(() -> new OrderNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));
    }

    Order create(final CreateNewOrderRequest request) {
        log.info("Creating a new order");

        return saveOrder(mapper.map(
                request,
                createOrderedProducts(request.getOrderedProducts()),
                DateUtils.nowAsInstant()
        ));
    }

    void delete(final UUID orderId) {
        repository.findByIdAndIsDeleted(orderId, false)
                .ifPresentOrElse(
                        order -> {
                            order.setDeleted(true);
                            saveOrder(order);
                        },
                        () -> {
                            throw new OrderNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(orderId));
                        });
    }

    void removeByIds(final Set<UUID> ids) {
        final var toRemove = repository.findAllByIdInAndIsDeleted(ids, false);
        if (toRemove.size() != ids.size()) {
            final var notFoundIds = toRemove.stream()
                    .map(Order::getId)
                    .filter(orderId -> !ids.contains(orderId))
                    .collect(Collectors.toSet());
            throw new OrderNotFoundException(OrderExceptionMessage.ORDERS_NOT_FOUND.withParameters(notFoundIds));
        }
        final var removed = toRemove.stream()
                .peek(order -> order.setDeleted(true))
                .collect(Collectors.toList());
        repository.saveAll(removed);
    }

    Order update(final UUID id, final UpdateOrderRequest updateOrder) {
        var existedOrder = repository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new OrderNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));

        final var allProductsAfterUpdate = new OrderProductsUpdater(existedOrder);
        allProductsAfterUpdate.update(updateOrder, this::createOrderedProducts);

        return saveOrder(mapper.map(updateOrder, existedOrder, allProductsAfterUpdate.values()));
    }

    private List<OrderedProduct> createOrderedProducts(final Collection<CreateNewOrderedProductRequest> newProductOrders) {
        if (CollectionUtils.isEmpty(newProductOrders)) {
            throw new OrderValidationException(OrderExceptionMessage.ORDER_PRODUCTS_CANNOT_BE_EMPTY);
        }
        if (!newProductOrders.stream().allMatch(product -> product.getQuantity() > 0.0)) {
            throw new OrderValidationException(OrderExceptionMessage.ORDER_PRODUCT_QUANTITY_MUST_BE_GREATER_THAN_ZERO);
        }
        final var productIds = newProductOrders.stream().map(CreateNewOrderedProductRequest::getProductId).collect(Collectors.toSet());
        final var foundProductsByIds = productService.findAllByIds(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        if (foundProductsByIds.size() != productIds.size()) {
            final var missingIds = productIds.stream()
                    .filter(id -> !foundProductsByIds.containsKey(id))
                    .collect(Collectors.toSet());
            log.error("Not all products were found and cannot finalize the ordered products. Missing products ids: {}", missingIds);
            throw new OrderValidationException(OrderExceptionMessage.ORDER_NOT_ALL_PRODUCTS_FOUND.withParameters(missingIds));
        }

        final var preparedProductOrders = newProductOrders.stream()
                .map(orderedProduct -> mapper.map(orderedProduct, foundProductsByIds.get(orderedProduct.getProductId())))
                .collect(Collectors.toList());
        log.info("{} ordered products has been created", preparedProductOrders.size());
        return preparedProductOrders;
    }

    private JpaSort jpaSort(final Sort.Order sort) {
        return JpaSort.unsafe(sort.getDirection(), sort.getProperty());
    }

    private Order saveOrder(final Order order) {
        return repository.save(order);
    }

}
