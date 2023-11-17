package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.order.exception.OrderCreationException;
import mm.expenses.manager.order.order.exception.OrderValidationException;
import mm.expenses.manager.order.order.model.Order;
import mm.expenses.manager.order.order.model.Order.OrderedProduct;
import mm.expenses.manager.order.product.ProductService;
import mm.expenses.manager.order.product.model.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class OrderCreator {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderValidator validator;
    private final ProductService productService;

    Optional<Order> create(final CreateNewOrderRequest newOrder) {
        log.info("Creating a new order");
        try {
            final var newOrderedProducts = newOrder.getOrderedProducts();
            if (CollectionUtils.isEmpty(newOrderedProducts)) {
                log.error("There is no at least 1 ordered product and order could not be finalized");
                throw new OrderCreationException("There should be at least 1 ordered product to finalize the order creation");
            }
            validator.checkIfObjectIsValid(validator.validateNew(newOrder), OrderValidationException.class);

            final var orderedProducts = createOrderedProducts(newOrderedProducts);
            final var preparedData = mapper.map(newOrder, orderedProducts, DateUtils.nowAsInstant());
            final var saved = repository.save(mapper.map(preparedData));
            return Optional.of(saved).map(mapper::map);
        } catch (final OrderValidationException exception) {
            log.warn("Order cannot be created because of validation failures: {}", exception.getMessage(), exception);
            throw new OrderCreationException("Order cannot be created because of validation failures: " + exception.getMessage(), exception);
        } catch (final Exception exception) {
            log.warn("Error occurred during new order creation: {}", exception.getMessage());
            throw new OrderCreationException("Order cannot be created because of: " + exception.getMessage(), exception);
        }
    }

    List<OrderedProduct> createOrderedProducts(final List<CreateNewOrderedProductRequest> newProductOrders) {
        final var productIds = newProductOrders.stream().map(CreateNewOrderedProductRequest::getProductId).collect(Collectors.toSet());
        final var foundProductsByIds = productService.findAllByIds(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        if (foundProductsByIds.size() != productIds.size()) {
            final var missingIds = productIds.stream()
                    .filter(id -> !foundProductsByIds.containsKey(id))
                    .collect(Collectors.toSet());
            log.error("Not all products were found and cannot finalize the ordered products. Missing products ids: {}", missingIds);
            throw new OrderCreationException("Not all products were found to create ordered products: " + missingIds);
        }

        final var preparedProductOrders = newProductOrders.stream()
                .map(orderedProduct -> mapper.map(orderedProduct, foundProductsByIds.get(orderedProduct.getProductId())))
                .collect(Collectors.toList());
        log.info("{} ordered products has been created", preparedProductOrders.size());
        return preparedProductOrders;
    }

}
