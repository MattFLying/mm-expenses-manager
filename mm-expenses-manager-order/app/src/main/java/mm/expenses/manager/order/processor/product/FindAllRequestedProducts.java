package mm.expenses.manager.order.processor.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ordered products strategy responsible for finding all requested ordered products.
 */
@Slf4j
@RequiredArgsConstructor
public class FindAllRequestedProducts implements OrderedProductStrategy<Map<UUID, Product>> {

    private final ProductService productService;
    private final Set<UUID> requestedProductIds;
    private boolean executed = false;

    @Override
    public Map<UUID, Product> execute() {
        final var foundProductsByIds = productService.findAllByIds(requestedProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        if (foundProductsByIds.size() != requestedProductIds.size()) {
            final var missingIds = requestedProductIds.stream()
                    .filter(productId -> !foundProductsByIds.containsKey(productId))
                    .collect(Collectors.toSet());
            log.error("Not all products were found and cannot finalize the ordered products. Missing products ids: {}", missingIds);
            throw new ApiBadRequestException(OrderExceptionMessage.ORDER_NOT_ALL_PRODUCTS_FOUND.withParameters(missingIds));
        }
        executed = true;
        return foundProductsByIds;
    }

    @Override
    public boolean executed() {
        return executed;
    }

}
