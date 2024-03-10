package mm.expenses.manager.order.order;

import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.exception.OrderValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ordered products updater.
 */
class OrderProductsUpdater extends HashMap<UUID, OrderedProduct> {

    private final Map<UUID, OrderedProduct> existingProductsById;
    private final Map<UUID, CreateNewOrderedProductRequest> newProducts;

    OrderProductsUpdater(final Order existedOrder) {
        existingProductsById = existedOrder.getProducts().stream().collect(Collectors.toMap(OrderedProduct::getId, Function.identity()));
        newProducts = new HashMap<UUID, CreateNewOrderedProductRequest>();
    }

    void update(final UpdateOrderRequest updateOrder, final NewProductAddAfterUpdateLocalized updater) {
        updateAlreadyExistedProductsOrAddAsNew(updateOrder, updater);
        removeProducts(updateOrder);
        validateAfterUpdate();
    }

    private void updateAlreadyExistedProductsOrAddAsNew(final UpdateOrderRequest updateOrder, final NewProductAddAfterUpdateLocalized updater) {
        addNewProductsToUpdatedOrder(updateOrder);
        if (CollectionUtils.isNotEmpty(updateOrder.getOrderedProducts())) {
            updateOrder.getOrderedProducts().stream()
                    .filter(toUpdate -> existingProductsById.containsKey(toUpdate.getProductId()))
                    .forEach(toUpdate -> put(toUpdate.getProductId(), update(existingProductsById.get(toUpdate.getProductId()), toUpdate)));

            // if product to update does not exists in order and is not market as new product then threat it as a new product
            updateOrder.getOrderedProducts().stream()
                    .filter(toUpdate -> !existingProductsById.containsKey(toUpdate.getProductId()))
                    .filter(toUpdate -> !newProducts.containsKey(toUpdate.getProductId()))
                    .map(newProduct -> new CreateNewOrderedProductRequest(newProduct.getProductId(), newProduct.getQuantity()))
                    .forEach(newProduct -> newProducts.put(newProduct.getProductId(), newProduct));
        }

        if (MapUtils.isNotEmpty(newProducts)) {
            updater.createNewProducts(newProducts.values()).forEach(newProduct -> put(newProduct.getId(), newProduct));
        }
        assignOriginalProductsIfNotPresentInUpdate();
    }

    private void addNewProductsToUpdatedOrder(final UpdateOrderRequest updateOrder) {
        if (CollectionUtils.isNotEmpty(updateOrder.getNewProducts())) {
            updateOrder.getNewProducts().stream()
                    .filter(newProduct -> !existingProductsById.containsKey(newProduct.getProductId()))
                    .forEach(newProduct -> newProducts.put(newProduct.getProductId(), newProduct));
        }
    }

    private OrderedProduct update(final OrderedProduct productFromEntity, final UpdateOrderedProductRequest updateOrderedProduct) {
        if (productFromEntity.getQuantity().equals(updateOrderedProduct.getQuantity())) {
            return productFromEntity;
        }
        return productFromEntity.toBuilder().quantity(updateOrderedProduct.getQuantity()).lastModifiedAt(Instant.now()).build();
    }

    private void assignOriginalProductsIfNotPresentInUpdate() {
        existingProductsById.values().stream()
                .filter(notUpdatedProduct -> !containsKey(notUpdatedProduct.getId()))
                .forEach(notUpdatedProduct -> put(notUpdatedProduct.getId(), notUpdatedProduct));
    }

    private void removeProducts(final UpdateOrderRequest updateOrder) {
        if (CollectionUtils.isNotEmpty(updateOrder.getRemoveProducts())) {
            updateOrder.getRemoveProducts().forEach(this::remove);
        }
    }

    private void validateAfterUpdate() {
        if (!values().stream().allMatch(product -> product.getQuantity() > 0.0)) {
            throw new OrderValidationException(OrderExceptionMessage.ORDER_PRODUCT_QUANTITY_MUST_BE_GREATER_THAN_ZERO);
        }
    }

    @FunctionalInterface
    public interface NewProductAddAfterUpdateLocalized {

        List<OrderedProduct> createNewProducts(final Collection<CreateNewOrderedProductRequest> newProduct);

    }

}
