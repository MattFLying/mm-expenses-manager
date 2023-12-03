package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderedProductRequest;
import mm.expenses.manager.order.order.OrderEntity.OrderedProductEntity;
import mm.expenses.manager.order.order.exception.OrderUpdateException;
import mm.expenses.manager.order.order.exception.OrderValidationException;
import mm.expenses.manager.order.order.model.*;
import mm.expenses.manager.order.order.model.Order.OrderedProduct;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class OrderUpdater {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderValidator validator;
    private final OrderCreator orderCreator;

    Optional<Order> update(final OrderEntity entity, final UpdateOrderRequest updateOrder) {
        final var id = entity.getId();
        log.info("Updating order of id: {}", id);
        try {
            validator.checkIfObjectIsValid(validator.validateUpdate(updateOrder), OrderValidationException.class);

            final var orderedProducts = updateOrderedProducts(entity, updateOrder);
            final var preparedData = mapper.map(updateOrder, entity, orderedProducts);
            final var saved = repository.save(mapper.map(preparedData));
            log.info("Order of id: {} has been updated", saved.getId());

            return Optional.of(saved).map(mapper::map);
        } catch (final OrderValidationException exception) {
            log.warn("Product order of id: {} cannot be updated because of validation failures: {}", id, exception.getMessage(), exception);
            throw new OrderUpdateException("Product order of id: " + id + " cannot be updated because of validation failures: " + exception.getMessage(), exception);
        } catch (final Exception exception) {
            log.warn("Error occurred during updating the order of id {}: {}", id, exception.getMessage());
            throw new OrderUpdateException("Order of id: " + id + " cannot be updated because of: " + exception.getMessage(), exception);
        }
    }

    private List<OrderedProduct> updateOrderedProducts(final OrderEntity entity, final UpdateOrderRequest updateOrder) {
        final var updatedOrderedProducts = updateOrderedProducts(entity.getOrderedProducts(), updateOrder.getOrderedProducts());
        final var allOrderedProducts = new ArrayList<>(updatedOrderedProducts);

        final var newOrders = updateOrder.getNewProducts();
        if (CollectionUtils.isNotEmpty(newOrders)) {
            log.info("There are a {} new products to order, data preparing", newOrders.size());
            allOrderedProducts.addAll(orderCreator.createOrderedProducts(newOrders));
        }
        return mergeSameOrderedProducts(allOrderedProducts);
    }

    private List<OrderedProduct> updateOrderedProducts(final List<OrderedProductEntity> productsFromEntity, final List<UpdateOrderedProductRequest> updatedProducts) {
        final var updatedByIds = updatedProducts.stream().collect(Collectors.toMap(UpdateOrderedProductRequest::getId, Function.identity()));
        final var result = productsFromEntity.stream()
                .filter(productFromEntity -> updatedByIds.containsKey(productFromEntity.getId()))
                .map(productFromEntity -> update(productFromEntity, updatedByIds.get(productFromEntity.getId())))
                .map(mapper::map)
                .collect(Collectors.toList());
        log.info("{} ordered products were updated", result.size());
        return result;
    }

    private OrderedProductEntity update(final OrderedProductEntity productFromEntity, final UpdateOrderedProductRequest updateOrderedProduct) {
        if (productFromEntity.getQuantity().equals(updateOrderedProduct.getQuantity())) {
            return productFromEntity;
        }
        return productFromEntity.toBuilder().quantity(updateOrderedProduct.getQuantity()).lastModifiedAt(Instant.now()).build();
    }

    private List<OrderedProduct> mergeSameOrderedProducts(final List<OrderedProduct> allOrderedProducts) {
        final var allProducts = new ArrayList<OrderedProduct>();
        final var groupedByProductId = allOrderedProducts.stream()
                .collect(Collectors.groupingBy(orderedProduct -> orderedProduct.getBoughtProduct().getProductId(), Collectors.toList()))
                .entrySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                entry -> entry.getValue().size() > 1 ? OrderForTheSameProduct.YES : OrderForTheSameProduct.NO,
                                Collectors.toList()
                        )
                );

        allProducts.addAll(
                groupedByProductId.getOrDefault(OrderForTheSameProduct.NO, Collections.emptyList())
                        .stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Collection::stream)
                        .toList()
        );
        allProducts.addAll(
                groupedByProductId.getOrDefault(OrderForTheSameProduct.YES, Collections.emptyList())
                        .stream()
                        .map(Map.Entry::getValue)
                        .map(this::mergeOrdersForTheSameProduct)
                        .flatMap(Collection::stream)
                        .toList()
        );
        return allProducts;
    }

    private Collection<OrderedProduct> mergeOrdersForTheSameProduct(final List<OrderedProduct> probablyDuplicates) {
        return probablyDuplicates.stream().collect(Collectors.toMap(OrderedProduct::getBoughtProduct, Function.identity(), OrderedProduct::mergeForSameProduct)).values();
    }

    private enum OrderForTheSameProduct {
        YES, NO
    }

}
