package mm.expenses.manager.order.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.core.*;
import mm.expenses.manager.order.exception.OrderExceptionMessage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Step in chain of {@link Order} update.
 * Summarizes order data after modifications.
 */
@Slf4j
@RequiredArgsConstructor
final class OrderSummaryUpdateDuringUpdate extends UpdateOrderChain {

    private final OrderRepository repository;
    private final Instant modificationTime;

    @Override
    public Order handleRequest(final UpdateOrderRequest request) {
        try {
            final var orderOpt = context.getArgument(FindOrderToUpdate.ORDER_KEY);
            final var areProductsUpdated = (boolean) context.getArgument(ModifyOrderedProductsDuringOrderUpdate.PRODUCTS_UPDATED_KEY).orElse(false);

            orderOpt.ifPresentOrElse(
                    order -> {
                        var isUpdated = (boolean) context.getArgument(UpdateBasicOrderData.IS_UPDATED_KEY).orElse(false);

                        if (areProductsUpdated) {
                            calculateOrderPrices((Order) order, modificationTime);
                            isUpdated = true;
                            context.addArgument(UpdateBasicOrderData.IS_UPDATED_KEY, isUpdated);
                        }

                        if (isUpdated) {
                            ((Order) order).setLastModifiedAt(modificationTime);
                        }

                        context.addArgument(FindOrderToUpdate.ORDER_KEY, order);
                    },
                    () -> {
                        log.error("Cannot update order because of one of required parameters is null. orderOpt={}", orderOpt);
                        throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED);
                    }
            );

            if (!hasNext()) {
                setNextHandler(new SaveUpdatedOrder(repository));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

    private void calculateOrderPrices(final Order existedOrder, final Instant updatedTime) {
        final var existingPrices = existedOrder.getPrices();
        final var existingPricesByCurrency = existingPrices.stream()
                .collect(Collectors.toMap(
                        OrderPrice::getCurrency, Function.identity()
                ));
        final var existingProductsByCurrency = existedOrder.getProducts()
                .stream()
                .collect(Collectors.groupingBy(
                        product -> product.getPrices()
                                .stream()
                                .filter(OrderedProductPrice::isPriceOriginal)
                                .findAny()
                                .map(OrderedProductPrice::getCurrency)
                                .orElse(CurrencyCode.UNDEFINED)
                ));

        existingPrices.removeIf(existingPrice -> !existingProductsByCurrency.containsKey(existingPrice.getCurrency()));
        existingProductsByCurrency.forEach((currency, products) -> updatePricesIfNeeded(existedOrder, updatedTime, currency, products, existingPricesByCurrency, existingPrices));
    }

    private void updatePricesIfNeeded(
            final Order existedOrder,
            final Instant updatedTime,
            final CurrencyCode currency,
            final List<OrderedProduct> products,
            final Map<CurrencyCode, OrderPrice> existingPricesByCurrency,
            final List<OrderPrice> existingPrices) {

        if (existingPricesByCurrency.containsKey(currency)) {
            final var price = existingPricesByCurrency.get(currency);
            price.setValue(getPricesSummary(products, currency));
            price.setLastModifiedAt(updatedTime);
            price.setDate(DateUtils.instantToLocalDate(updatedTime).toString());
        } else {
            existingPrices.add(createOrderPrice(existedOrder, updatedTime, currency, products));
        }
    }

    private BigDecimal getPricesSummary(final List<OrderedProduct> products, final CurrencyCode currency) {
        return products.stream()
                .map(orderedProduct -> orderedProduct.getPriceSummary(currency))
                .findAny()
                .map(Price::getValue)
                .orElse(BigDecimal.ZERO);
    }

    private OrderPrice createOrderPrice(final Order existedOrder, final Instant updatedTime, final CurrencyCode currency, final List<OrderedProduct> products) {
        return OrderPrice.builder()
                .value(getPricesSummary(products, currency))
                .currency(currency)
                .createdAt(updatedTime)
                .lastModifiedAt(updatedTime)
                .date(DateUtils.instantToLocalDate(updatedTime).toString())
                .order(existedOrder)
                .build();
    }

}
