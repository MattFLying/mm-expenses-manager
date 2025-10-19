package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.price.OrderPrice;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for new {@link Order} creation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrderCreator {

    private final OrderedProductService orderedProductService;

    Order create(final CreateNewOrderRequest request) {
        val orderedProductsRequest = request.getOrderedProducts();
        if (CollectionUtils.isEmpty(orderedProductsRequest)) {
            throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCTS_CANNOT_BE_EMPTY);
        }

        val requestedProductIds = orderedProductsRequest.stream()
                .map(CreateNewOrderedProductRequest::getProductId)
                .collect(Collectors.toSet());

        val foundProductsByIds = orderedProductService.findAllRequestedProducts(requestedProductIds);
        val orderedProducts = orderedProductsRequest.stream()
                .map(orderedProduct -> orderedProductService.mapToOrderedProduct(orderedProduct, foundProductsByIds.get(orderedProduct.getProductId())))
                .collect(Collectors.toList());

        val orderPrices = new ArrayList<OrderPrice>();
        calculateOrderPrices(orderedProducts, orderPrices);

        return Order.builder()
                .name(request.getName())
                .products(orderedProducts)
                .prices(orderPrices)
                .build();
    }

    private void calculateOrderPrices(final List<OrderedProduct> orderedProducts, final List<OrderPrice> orderPrices) {
        orderedProducts.stream()
                .collect(Collectors.groupingBy(
                        OrderedProduct::getCurrency
                ))
                .forEach((currency, products) -> {
                    orderPrices.add(orderedProductService.calculateOrderPrice(currency, products));
                });
    }

}
