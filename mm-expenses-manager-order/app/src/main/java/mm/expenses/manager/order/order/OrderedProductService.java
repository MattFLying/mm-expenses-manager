package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.price.PriceConverter;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductPrice;
import mm.expenses.manager.order.product.ProductService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Responsible for {@link OrderedProduct} operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrderedProductService {

    private final ProductService productService;
    private final PriceConverter priceConverter;

    OrderedProduct mapToOrderedProduct(final CreateNewOrderedProductRequest request, final Product product, final Instant date) {
        return createOrderedProduct(request, product, date);
    }

    Map<UUID, Product> findAllRequestedProducts(final Set<UUID> requestedProductIds) {
        val foundProductsByIds = productService.findAllByIds(requestedProductIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        if (foundProductsByIds.size() != requestedProductIds.size()) {
            val missingIds = requestedProductIds.stream()
                    .filter(productId -> !foundProductsByIds.containsKey(productId))
                    .collect(Collectors.toSet());
            log.error("Not all products were found and cannot finalize the ordered products. Missing products ids: {}", missingIds);
            throw new ApiValidationException(OrderExceptionMessage.ORDER_NOT_ALL_PRODUCTS_FOUND.withParameters(missingIds));
        }
        return foundProductsByIds;
    }

    OrderPrice calculateOrderPrice(final CurrencyCode currency, final List<OrderedProduct> products, final Instant creationDate) {
        return OrderPrice.builder()
                .value(priceConverter.getPricesSummary(products, currency))
                .currency(currency)
                .isPriceOriginal(true)
                .isPriceConverted(false)
                .createdAt(creationDate)
                .lastModifiedAt(creationDate)
                .date(DateUtils.instantToLocalDate(creationDate).toString())
                .build();
    }

    OrderPrice createOrderPrice(final Order existedOrder, final Instant updatedTime, final CurrencyCode currency, final List<OrderedProduct> products) {
        return OrderPrice.builder()
                .value(priceConverter.getPricesSummary(products, currency))
                .currency(currency)
                .createdAt(updatedTime)
                .lastModifiedAt(updatedTime)
                .date(DateUtils.instantToLocalDate(updatedTime).toString())
                .order(existedOrder)
                .build();
    }

    private OrderedProduct createOrderedProduct(final CreateNewOrderedProductRequest request, final Product product, final Instant date) {
        val orderedProduct = new OrderedProduct();
        orderedProduct.setProduct(product);
        orderedProduct.setQuantity(request.getQuantity());

        val price = new OrderedProductPrice();
        price.setOrderedProduct(orderedProduct);
        price.setPriceOriginal(true);
        price.setPriceConverted(false);

        if (Objects.nonNull(date)) {
            orderedProduct.setCreatedAt(date);
            orderedProduct.setLastModifiedAt(date);

            price.setCreatedAt(date);
            price.setLastModifiedAt(date);
            price.setDate(DateUtils.instantToLocalDate(date).toString());
        }

        val requestedPrice = request.getPrice();
        if (Objects.nonNull(requestedPrice)) {
            if (Objects.isNull(requestedPrice.getValue())) {
                throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCTS_CUSTOM_PRICE_VALUE_MISSING.withParameters(request.getProductId()));
            }

            val currency = Objects.isNull(requestedPrice.getCurrency())
                    ? priceConverter.getDefaultCurrency()
                    : CurrencyCode.getCurrencyFromString(requestedPrice.getCurrency());

            price.setValue(requestedPrice.getValue());
            price.setCurrency(currency);
            price.setPriceCustom(true);
        } else {
            val prices = product.getPrices();
            val productPrice = prices.stream()
                    .filter(ProductPrice::isOriginal)
                    .findAny()
                    .orElseGet(() -> {
                        val defaultCurrencyPrice = prices.get(priceConverter.getDefaultCurrency());

                        // in case if original price for this product does not exist, find the price of current
                        // default currency, otherwise returns the first available price
                        return Objects.nonNull(defaultCurrencyPrice) ? defaultCurrencyPrice : prices.get(0);
                    });

            price.setValue(productPrice.getValue());
            price.setCurrency(productPrice.getCurrency());
            price.setPriceCustom(false);
        }

        val prices = new ArrayList<OrderedProductPrice>();
        prices.add(price);

        orderedProduct.setPrices(prices);
        return orderedProduct;
    }

}
