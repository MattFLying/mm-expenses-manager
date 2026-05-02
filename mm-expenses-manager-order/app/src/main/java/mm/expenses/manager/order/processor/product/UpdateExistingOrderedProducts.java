package mm.expenses.manager.order.processor.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.order.OrderCommonValidation;
import mm.expenses.manager.order.api.order.model.PriceRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderedProduct;
import mm.expenses.manager.order.core.OrderedProductPrice;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Update existing ordered products' strategy.
 */
@RequiredArgsConstructor
public class UpdateExistingOrderedProducts implements OrderedProductStrategy<Order> {

    private final Order order;
    private final List<UpdateOrderedProductRequest> request;
    private final Instant modificationTime;
    private boolean executed = false;

    @Override
    public Order execute() {
        final var existingProducts = order.getProducts();
        final var existingProductsById = existingProducts.stream()
                .collect(Collectors.toMap(
                        OrderedProduct::getId,
                        Function.identity()
                ));
        final var requestById = request.stream()
                .collect(Collectors.toMap(
                        UpdateOrderedProductRequest::getOrderedProductId,
                        Function.identity()
                ));
        validateIfProductsExist(requestById, existingProductsById);
        validateProductsQuantity(request);

        for (var orderedProduct : existingProducts) {
            final var orderedProductId = orderedProduct.getId();
            if (requestById.containsKey(orderedProductId)) {
                final var toUpdate = requestById.get(orderedProductId);
                final var updatedProduct = updateExistingOrderedProduct(orderedProduct, toUpdate, modificationTime);
                if (!executed && updatedProduct) {
                    executed = true;
                }
            }
        }
        return order;
    }

    @Override
    public boolean executed() {
        return executed;
    }

    private void validateIfProductsExist(final Map<UUID, UpdateOrderedProductRequest> requestById, final Map<UUID, OrderedProduct> existingProductsById) {
        final var notFoundOrderedProducts = new HashSet<UUID>();
        requestById.forEach((orderedProductId, orderedProductRequest) -> {
            if (!existingProductsById.containsKey(orderedProductId)) {
                notFoundOrderedProducts.add(orderedProductId);
            }
        });
        if (!notFoundOrderedProducts.isEmpty()) {
            throw new ApiNotFoundException(OrderExceptionMessage.ORDERED_PRODUCTS_NOT_FOUND.withParameters(String.format("%s", notFoundOrderedProducts)));
        }
    }

    private void validateProductsQuantity(final List<UpdateOrderedProductRequest> request) {
        final var wrongQuantities = request.stream()
                .map(product -> OrderCommonValidation.validateProductQuantity(product.getOrderedProductId(), product.getQuantity()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!wrongQuantities.isEmpty()) {
            throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCT_QUANTITY_MUST_BE_GREATER_THAN_ZERO.withParameters(wrongQuantities));
        }
    }

    private boolean updateExistingOrderedProduct(final OrderedProduct productFromEntity, final UpdateOrderedProductRequest requestedProduct, final Instant updateTime) {
        var isProductUpdated = false;

        if (Objects.nonNull(requestedProduct.getQuantity())) {
            updateQuantity(productFromEntity, requestedProduct, updateTime, isProductUpdated);
        }

        if (Objects.nonNull(requestedProduct.getPrice())) {
            final var newPrice = requestedProduct.getPrice();
            final var prices = productFromEntity.getPrices();

            updatePriceValue(productFromEntity, updateTime, newPrice, prices, isProductUpdated);
            updatePriceCurrency(productFromEntity, updateTime, newPrice, prices, isProductUpdated);
        }
        return isProductUpdated;
    }

    private void updateQuantity(final OrderedProduct productFromEntity, final UpdateOrderedProductRequest requestedProduct, final Instant updateTime, boolean isProductUpdated) {
        final var isQuantityValid = OrderCommonValidation.validateProductQuantity(productFromEntity.getId(), requestedProduct.getQuantity());
        if (Objects.nonNull(isQuantityValid)) {
            throw new ApiValidationException(isQuantityValid);
        }

        productFromEntity.setQuantity(requestedProduct.getQuantity());
        productFromEntity.setLastModifiedAt(updateTime);

        isProductUpdated = true;
    }

    private void updatePriceValue(final OrderedProduct productFromEntity, final Instant updateTime, final PriceRequest newPrice, final List<OrderedProductPrice> prices, boolean isProductUpdated) {
        if (Objects.nonNull(newPrice.getValue())) {
            final var originalPrice = prices.stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findFirst()
                    .orElse(null);

            if (Objects.nonNull(originalPrice)) {
                originalPrice.setValue(newPrice.getValue());
                originalPrice.setPriceCustom(true);
                originalPrice.setLastModifiedAt(updateTime);

                productFromEntity.setLastModifiedAt(updateTime);
                isProductUpdated = true;
            }
        }
    }

    private void updatePriceCurrency(final OrderedProduct productFromEntity, final Instant updateTime, final PriceRequest newPrice, final List<OrderedProductPrice> prices, boolean isProductUpdated) {
        if (Objects.nonNull(newPrice.getCurrency())) {
            final var originalPrice = prices.stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findFirst()
                    .orElse(null);

            if (Objects.nonNull(originalPrice)) {
                originalPrice.setCurrency(CurrencyCode.getCurrencyFromString(newPrice.getCurrency()));
                originalPrice.setPriceCustom(true);
                originalPrice.setLastModifiedAt(updateTime);

                productFromEntity.setLastModifiedAt(updateTime);
                isProductUpdated = true;
            }
        }
    }

}
