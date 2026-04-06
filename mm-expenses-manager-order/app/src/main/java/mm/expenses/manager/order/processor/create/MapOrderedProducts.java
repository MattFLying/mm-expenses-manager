package mm.expenses.manager.order.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.*;
import mm.expenses.manager.order.product.Product;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Step in chain of new {@link Order} creation.
 * Maps found requested products to new {@link OrderedProduct} associated with the expected order.
 */
@Slf4j
@RequiredArgsConstructor
final class MapOrderedProducts extends CreateOrderChain {

    final static String ORDERED_PRODUCTS_KEY = "preparedOrderedProducts";

    private final OrderRepository repository;
    private final OrderedProductService productService;
    private final Instant creationTime;

    @Override
    public Order handleRequest(final CreateNewOrderRequest request) {
        try {
            final var orderedProductsRequestOpt = context.getArgument(FindOrderedProducts.REQUESTED_PRODUCTS_KEY);
            final var foundProductsByIdsOpt = context.getArgument(FindOrderedProducts.FOUND_PRODUCTS_BY_ID_KEY);

            if (orderedProductsRequestOpt.isPresent() && foundProductsByIdsOpt.isPresent()) {
                final var requestedOrderedProducts = (List<CreateNewOrderedProductRequest>) orderedProductsRequestOpt.get();
                final var foundProductsByIds = (Map<UUID, Product>) foundProductsByIdsOpt.get();

                final var orderedProducts = requestedOrderedProducts.stream()
                        .map(orderedProduct -> createOrderedProduct(orderedProduct, foundProductsByIds))
                        .collect(Collectors.toList());

                context.addArgument(ORDERED_PRODUCTS_KEY, orderedProducts);
            } else {
                log.error("Cannot create order because of one of required parameters is null. orderedProductsRequestOpt={}, foundProductsByIdsOpt={}", orderedProductsRequestOpt, foundProductsByIdsOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED);
            }

            if (!hasNext()) {
                setNextHandler(new CalculatePrices(repository, creationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order creation error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        }
    }

    private OrderedProduct createOrderedProduct(final CreateNewOrderedProductRequest orderedProduct, final Map<UUID, Product> foundProductsByIds) {
        return productService.create(orderedProduct, foundProductsByIds.get(orderedProduct.getProductId()), creationTime);
    }

}
