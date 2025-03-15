package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.currency.PriceConverter;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    private final PriceConverter priceConverter;
    private final OrderSpecificationHandler specificationHandler;

    Page<Order> findOrders(final EntityFilter queryFilter) {
        Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

        val filterParameters = queryFilter.buildQueryParams();
        val specificationResult = specificationHandler.handle(filterParameters);
        val pagedOrders = repository.findAll(specificationResult.specification(), specificationResult.pageable());
        if (queryFilter.shouldConvertCurrenciesToDefault()) {
            val productsByOrderId = pagedOrders.getContent()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Order::getId,
                            Collectors.flatMapping(order -> order.getProducts().stream(), Collectors.toList())
                    ));

            // calculate prices if different currencies
            val isCurrencyConversionNeeded = productsByOrderId.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .anyMatch(orderedProduct -> !orderedProduct.getPrice().containsCurrency(priceConverter.getDefaultCurrency()));
            if (isCurrencyConversionNeeded) {
                val convertedOrders = priceConverter.convertPricesByOrderId(productsByOrderId);
                pagedOrders.getContent()
                        .forEach(order -> {
                            val orderId = order.getId();
                            val productsByOrder = convertedOrders.getOrDefault(orderId, order.getProducts());

                            order.setProducts(productsByOrder);
                            order.setPriceSummary(Prices.calculatePriceSummary(productsByOrder));
                        });
            }
        }
        return pagedOrders;
    }

    Order findById(final UUID id, final Boolean isDeleted, final Boolean shouldConvertCurrency) {
        val isDeletedFlag = Objects.nonNull(isDeleted) ? isDeleted : false;
        val foundOrder = repository.findByIdAndIsDeleted(id, isDeletedFlag)
                .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));

        pricesConversion(shouldConvertCurrency, foundOrder);
        return foundOrder;
    }

    Order create(final CreateNewOrderRequest request, final Boolean shouldConvertCurrency) {
        log.info("Creating a new order");

        val creationTime = DateUtils.nowAsInstant();
        val savedOrder = saveOrder(mapper.map(
                request,
                createOrderedProducts(request.getOrderedProducts(), creationTime),
                creationTime
        ));
        pricesConversion(shouldConvertCurrency, savedOrder);
        return savedOrder;
    }

    void delete(final UUID orderId) {
        repository.findByIdAndIsDeleted(orderId, false)
                .ifPresentOrElse(
                        order -> {
                            order.setDeleted(true);
                            saveOrder(order);
                        },
                        () -> {
                            throw new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(orderId));
                        });
    }

    void removeByIds(final Set<UUID> ids) {
        val toRemove = repository.findAllByIdInAndIsDeleted(ids, false);
        if (toRemove.size() != ids.size()) {
            val notFoundIds = toRemove.stream()
                    .map(Order::getId)
                    .filter(orderId -> !ids.contains(orderId))
                    .collect(Collectors.toSet());
            throw new ApiNotFoundException(OrderExceptionMessage.ORDERS_NOT_FOUND.withParameters(notFoundIds));
        }
        val removed = toRemove.stream()
                .peek(order -> order.setDeleted(true))
                .collect(Collectors.toList());
        repository.saveAll(removed);
    }

    Order update(final UUID id, final UpdateOrderRequest updateOrder, final Boolean shouldConvertCurrency) {
        var existedOrder = repository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));

        val modifiedAt = DateUtils.nowAsInstant();
        val allProductsAfterUpdate = new OrderProductsUpdater(existedOrder);
        allProductsAfterUpdate.update(updateOrder, newProductOrders -> createOrderedProducts(newProductOrders, modifiedAt));

        val updatedOrder = saveOrder(mapper.map(
                updateOrder,
                existedOrder,
                allProductsAfterUpdate.values(),
                modifiedAt
        ));
        pricesConversion(shouldConvertCurrency, updatedOrder);
        return updatedOrder;
    }

    private List<OrderedProduct> createOrderedProducts(final Collection<CreateNewOrderedProductRequest> newProductOrders, final Instant creationTime) {
        if (CollectionUtils.isEmpty(newProductOrders)) {
            throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCTS_CANNOT_BE_EMPTY);
        }
        if (!newProductOrders.stream().allMatch(product -> product.getQuantity() > 0.0)) {
            throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCT_QUANTITY_MUST_BE_GREATER_THAN_ZERO);
        }
        val productIds = newProductOrders.stream().map(CreateNewOrderedProductRequest::getProductId).collect(Collectors.toSet());
        val foundProductsByIds = productService.findAllByIds(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        if (foundProductsByIds.size() != productIds.size()) {
            val missingIds = productIds.stream()
                    .filter(id -> !foundProductsByIds.containsKey(id))
                    .collect(Collectors.toSet());
            log.error("Not all products were found and cannot finalize the ordered products. Missing products ids: {}", missingIds);
            throw new ApiValidationException(OrderExceptionMessage.ORDER_NOT_ALL_PRODUCTS_FOUND.withParameters(missingIds));
        }

        val preparedProductOrders = newProductOrders.stream()
                .map(orderedProduct -> mapper.map(orderedProduct, foundProductsByIds.get(orderedProduct.getProductId()), creationTime))
                .collect(Collectors.toList());

        log.info("{} ordered products has been created", preparedProductOrders.size());
        return preparedProductOrders;
    }

    private void pricesConversion(final Boolean shouldConvertCurrency, final Order order) {
        if (Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency) {
            // calculate prices if different currencies to default currency
            val defaultCurrency = priceConverter.getDefaultCurrency();
            val isCurrencyConversionNeeded = order.getProducts()
                    .stream()
                    .anyMatch(orderedProduct -> !orderedProduct.getPrice().containsCurrency(defaultCurrency) || !orderedProduct.getPriceSummary().containsCurrency(defaultCurrency));
            if (isCurrencyConversionNeeded) {
                val convertedProducts = priceConverter.convertPrices(order.getProducts());
                order.setProducts(convertedProducts);
                order.setPriceSummary(Prices.calculatePriceSummary(convertedProducts));
            }
        }
    }

    private Order saveOrder(final Order order) {
        return repository.save(order);
    }

}
