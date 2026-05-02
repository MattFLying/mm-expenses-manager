package mm.expenses.manager.order.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.OrderedProductService;
import mm.expenses.manager.order.product.Product;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Step in chain of new {@link Order} creation.
 * Finds requested products by their ids.
 */
@Slf4j
@RequiredArgsConstructor
final class FindOrderedProducts extends CreateOrderChain {

    final static String REQUESTED_PRODUCTS_KEY = "requestedProducts";
    final static String FOUND_PRODUCTS_BY_ID_KEY = "foundProductsByIds";

    private final OrderRepository repository;
    private final OrderedProductService productService;
    private final Instant creationTime;

    @Override
    public Order handleRequest(final CreateNewOrderRequest request) {
        try {
            final var requestedProducts = request.getOrderedProducts();
            if (CollectionUtils.isEmpty(requestedProducts)) {
                throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCTS_CANNOT_BE_EMPTY);
            }

            final var requestedProductIds = getUniqueProductIds(requestedProducts);
            final var foundProductsByIds = productService.findAllRequestedProducts(requestedProductIds);
            updateContext(requestedProducts, foundProductsByIds);

            if (!hasNext()) {
                setNextHandler(new MapOrderedProducts(repository, productService, creationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order creation error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        }
    }

    private Set<UUID> getUniqueProductIds(final List<CreateNewOrderedProductRequest> orderedProductsRequest) {
        return orderedProductsRequest.stream()
                .map(CreateNewOrderedProductRequest::getProductId)
                .collect(Collectors.toSet());
    }

    private void updateContext(final List<CreateNewOrderedProductRequest> orderedProductsRequest, final Map<UUID, Product> foundProductsByIds) {
        context.addArguments(
                Map.of(
                        REQUESTED_PRODUCTS_KEY, orderedProductsRequest,
                        FOUND_PRODUCTS_BY_ID_KEY, foundProductsByIds
                )
        );
    }

}
