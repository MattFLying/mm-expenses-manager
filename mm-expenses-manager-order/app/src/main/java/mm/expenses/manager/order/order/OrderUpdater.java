package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.OrderCommonValidation;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.price.PriceConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Responsible for {@link Order} update process for the order itself and assigned {@link OrderedProduct}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrderUpdater {

    private final PriceConverter priceConverter;
    private final OrderedProductService orderedProductService;

    boolean update(final UpdateOrderRequest request, final Order existedOrder) {
        val updateTime = Instant.now();
        var isUpdated = updateOrderName(existedOrder, request);
        var areProductsUpdated = false;

        val existingProductsToUpdate = request.getOrderedProducts();
        if (CollectionUtils.isNotEmpty(existingProductsToUpdate)) {
            areProductsUpdated = updateExistingOrderedProducts(existedOrder, existingProductsToUpdate, updateTime);
        }

        val productsToRemove = request.getRemoveProducts();
        if (CollectionUtils.isNotEmpty(productsToRemove)) {
            areProductsUpdated = removeOrderedProducts(existedOrder, productsToRemove);
        }

        val productsToAdd = request.getNewProducts();
        if (CollectionUtils.isNotEmpty(productsToAdd)) {
            areProductsUpdated = addNewOrderedProducts(existedOrder, productsToAdd, updateTime);
        }

        if (areProductsUpdated) {
            calculateOrderPrices(existedOrder, updateTime);
            isUpdated = true;
        }

        if (isUpdated) {
            existedOrder.setLastModifiedAt(updateTime);
        }
        return isUpdated;
    }

    private boolean updateOrderName(final Order existedOrder, final UpdateOrderRequest request) {
        if (Objects.nonNull(request.getName())) {
            if (!OrderCommonValidation.isOrderNameNotEmpty(request.getName())) {
                throw new ApiValidationException(OrderExceptionMessage.ORDER_NAME_EMPTY);
            }
            existedOrder.setName(request.getName().trim());
            return true;
        }
        return false;
    }

    private boolean updateExistingOrderedProducts(final Order existedOrder, final List<UpdateOrderedProductRequest> request, final Instant updateTime) {
        var updatedProducts = false;

        val existingProducts = existedOrder.getProducts();
        val existingProductsById = existingProducts.stream()
                .collect(Collectors.toMap(
                        OrderedProduct::getId,
                        Function.identity()
                ));
        val requestById = request.stream()
                .collect(Collectors.toMap(
                        UpdateOrderedProductRequest::getOrderedProductId,
                        Function.identity()
                ));
        validateIfProductsExist(requestById, existingProductsById);
        validateProductsQuantity(request);

        for (var orderedProduct : existingProducts) {
            val orderedProductId = orderedProduct.getId();
            if (requestById.containsKey(orderedProductId)) {
                val toUpdate = requestById.get(orderedProductId);
                val updatedProduct = updateExistingOrderedProduct(orderedProduct, toUpdate, updateTime);
                if (!updatedProducts && updatedProduct) {
                    updatedProducts = true;
                }
            }
        }
        return updatedProducts;
    }

    private void validateIfProductsExist(final Map<UUID, UpdateOrderedProductRequest> requestById, final Map<UUID, OrderedProduct> existingProductsById) {
        val notFoundOrderedProducts = new HashSet<UUID>();
        requestById.forEach((orderedProductId, orderedProductRequest) -> {
            if (!existingProductsById.containsKey(orderedProductId)) {
                notFoundOrderedProducts.add(orderedProductId);
            }
        });
        if (!notFoundOrderedProducts.isEmpty()) {
            throw new ApiValidationException(OrderExceptionMessage.ORDERED_PRODUCTS_NOT_FOUND.withParameters(String.format("%s", notFoundOrderedProducts)));
        }
    }

    private void validateProductsQuantity(final List<UpdateOrderedProductRequest> request) {
        val wrongQuantities = request.stream()
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
            val isQuantityValid = OrderCommonValidation.validateProductQuantity(productFromEntity.getId(), requestedProduct.getQuantity());
            if (Objects.nonNull(isQuantityValid)) {
                throw new ApiValidationException(isQuantityValid);
            }

            productFromEntity.setQuantity(requestedProduct.getQuantity());

            productFromEntity.setLastModifiedAt(updateTime);
            isProductUpdated = true;
        }

        if (Objects.nonNull(requestedProduct.getPrice())) {
            val newPrice = requestedProduct.getPrice();
            val prices = productFromEntity.getPrices();
            if (Objects.nonNull(newPrice.getValue())) {
                val originalPrice = prices.stream()
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
            if (Objects.nonNull(newPrice.getCurrency())) {
                val originalPrice = prices.stream()
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
        return isProductUpdated;
    }

    private boolean removeOrderedProducts(final Order existedOrder, final List<UUID> productsToRemove) {
        val existingProducts = existedOrder.getProducts();
        return existingProducts.removeIf(orderedProduct -> productsToRemove.contains(orderedProduct.getId()));
    }

    private boolean addNewOrderedProducts(final Order existedOrder, final List<CreateNewOrderedProductRequest> productsToAdd, final Instant updatedTime) {
        var updatedProducts = false;

        val productIdsToAdd = productsToAdd.stream()
                .map(CreateNewOrderedProductRequest::getProductId)
                .collect(Collectors.toSet());
        val foundProductsByIds = orderedProductService.findAllRequestedProducts(productIdsToAdd);

        val newOrderedProducts = new ArrayList<OrderedProduct>();
        for (var productToAdd : productsToAdd) {
            val newOrderedProduct = orderedProductService.mapToOrderedProduct(productToAdd, foundProductsByIds.get(productToAdd.getProductId()), updatedTime);
            newOrderedProduct.setOrder(existedOrder);
            newOrderedProduct.setCreatedAt(updatedTime);
            newOrderedProduct.setLastModifiedAt(updatedTime);
            newOrderedProduct.setDeleted(false);

            newOrderedProducts.add(newOrderedProduct);
            updatedProducts = true;
        }

        if (updatedProducts) {
            existedOrder.getProducts().addAll(newOrderedProducts);
        }
        return updatedProducts;
    }

    private void calculateOrderPrices(final Order existedOrder, final Instant updatedTime) {
        val existingPrices = existedOrder.getPrices();
        val existingPricesByCurrency = existingPrices.stream()
                .collect(Collectors.toMap(
                        OrderPrice::getCurrency, Function.identity()
                ));
        val existingProductsByCurrency = existedOrder.getProducts()
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
        existingProductsByCurrency.forEach((currency, products) -> {
            if (existingPricesByCurrency.containsKey(currency)) {
                val price = existingPricesByCurrency.get(currency);
                price.setValue(priceConverter.getPricesSummary(products, currency));
                price.setLastModifiedAt(updatedTime);
                price.setDate(DateUtils.instantToLocalDate(updatedTime).toString());
            } else {
                existingPrices.add(orderedProductService.createOrderPrice(existedOrder, updatedTime, currency, products));
            }
        });
    }

}
