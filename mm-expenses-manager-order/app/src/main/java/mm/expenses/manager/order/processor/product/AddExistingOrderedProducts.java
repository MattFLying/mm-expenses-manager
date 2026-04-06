package mm.expenses.manager.order.processor.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderedProduct;
import mm.expenses.manager.order.processor.OrderedProductService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ordered products strategy responsible for add new products.
 */
@RequiredArgsConstructor
public class AddExistingOrderedProducts implements OrderedProductStrategy<Order> {

    private final Order order;
    private final List<CreateNewOrderedProductRequest> request;
    private final OrderedProductService productService;
    private final Instant modificationTime;
    private boolean executed = false;

    @Override
    public Order execute() {
        final var productIdsToAdd = request.stream()
                .map(CreateNewOrderedProductRequest::getProductId)
                .collect(Collectors.toSet());
        final var foundProductsByIds = productService.findAllRequestedProducts(productIdsToAdd);

        final var newOrderedProducts = new ArrayList<OrderedProduct>();
        for (var productToAdd : request) {
            final var newOrderedProduct = productService.create(productToAdd, foundProductsByIds.get(productToAdd.getProductId()), modificationTime);
            newOrderedProduct.setOrder(order);
            newOrderedProduct.setCreatedAt(modificationTime);
            newOrderedProduct.setLastModifiedAt(modificationTime);
            newOrderedProduct.setDeleted(false);

            newOrderedProducts.add(newOrderedProduct);
            executed = true;
        }

        if (executed) {
            order.getProducts().addAll(newOrderedProducts);
        }

        return order;
    }

    @Override
    public boolean executed() {
        return executed;
    }

}
