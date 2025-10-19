package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.price.PriceConverter;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final PriceConverter priceConverter;
    private final OrderUpdater updater;
    private final OrderCreator creator;
    private final OrderSpecificationHandler specificationHandler;

    @Transactional
    Order create(final CreateNewOrderRequest request, final Boolean shouldConvertCurrency) {
        val newOrder = creator.create(request);
        val saved = repository.save(newOrder);
        priceConverter.pricesConversion(shouldConvertCurrency, saved);

        return saved;
    }

    @Transactional
    Order update(final UUID id, final UpdateOrderRequest request, final Boolean shouldConvertCurrency) {
        val existedOrder = repository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));

        val isUpdated = updater.update(request, existedOrder);

        if (isUpdated) {
            val saved = repository.save(existedOrder);
            priceConverter.pricesConversion(shouldConvertCurrency, saved);

            return saved;
        }
        priceConverter.pricesConversion(shouldConvertCurrency, existedOrder);
        return existedOrder;
    }

    void delete(final UUID orderId) {
        repository.findByIdAndIsDeleted(orderId, false)
                .ifPresentOrElse(
                        order -> {
                            val deletedAt = DateUtils.nowAsInstant();
                            order.setDeleted(true);
                            order.setLastModifiedAt(deletedAt);

                            order.getProducts().forEach(product -> {
                                product.setDeleted(true);
                                product.setLastModifiedAt(deletedAt);
                            });
                            order.getPrices().forEach(price -> {
                                price.setDeleted(true);
                                price.setLastModifiedAt(deletedAt);
                            });

                            repository.save(order);
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

    Order findById(final UUID id, final Boolean isDeleted, final Boolean shouldConvertCurrency) {
        val isDeletedFlag = Objects.nonNull(isDeleted) ? isDeleted : false;
        val foundOrder = repository.findByIdAndIsDeleted(id, isDeletedFlag)
                .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));

        priceConverter.pricesConversion(shouldConvertCurrency, foundOrder);
        return foundOrder;
    }

    Page<Order> findOrders(final EntityFilter queryFilter) {
        Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

        val filterParameters = queryFilter.buildQueryParams();
        val specificationResult = specificationHandler.handle(filterParameters, queryFilter.getAdditionalCriteriaParametersAsArray());
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
                    .anyMatch(orderedProduct -> !Objects.equals(orderedProduct.getCurrency(), priceConverter.getDefaultCurrency()));
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

}
