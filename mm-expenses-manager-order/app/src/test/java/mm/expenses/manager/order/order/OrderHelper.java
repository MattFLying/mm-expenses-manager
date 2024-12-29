package mm.expenses.manager.order.order;

import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderedProductRequest;
import mm.expenses.manager.order.product.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class OrderHelper {

    private static final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    public static final UUID ID = UUID.randomUUID();
    public static final String ORDER_NAME = UUID.randomUUID().toString();
    public static final Double ORDER_QUANTITY = 2.0;
    public static final CurrencyCode DEFAULT_CURRENCY = CurrencyCode.PLN;
    public static final Map<String, Object> PRODUCT_DETAILS = Map.of("key", "value");

    public static CreateNewOrderRequest createOrderRequest(final List<CreateNewOrderedProductRequest> products, final String name) {
        var request = new CreateNewOrderRequest();
        request.setName(name);
        request.setOrderedProducts(products);

        return request;
    }

    public static CreateNewOrderRequest createOrderRequest(final String name, final Product product) {
        return createOrderRequest(name, product, ORDER_QUANTITY);
    }

    public static CreateNewOrderRequest createOrderRequest(final String name, final Product product, final Double quantity) {
        var request = new CreateNewOrderRequest();
        request.setName(name);

        if (Objects.nonNull(product)) {
            var newProduct = new CreateNewOrderedProductRequest();
            newProduct.setProductId(product.getId());
            newProduct.setQuantity(quantity);

            request.setOrderedProducts(List.of(newProduct));
        }
        return request;
    }

    public static CreateNewOrderRequest createOrderRequestSkipQuantity(final String name, final Product product, final boolean skipQuantity) {
        return createOrderRequest(name, product, skipQuantity ? null : ORDER_QUANTITY);
    }

    public static CreateNewOrderRequest createOrderRequestEmptyProductId(final String name) {
        var newProduct = new CreateNewOrderedProductRequest();
        newProduct.setProductId(null);
        newProduct.setQuantity(ORDER_QUANTITY);

        var request = new CreateNewOrderRequest();
        request.setName(name);
        request.setOrderedProducts(List.of(newProduct));

        return request;
    }

    public static Order createOrderFromOrderRequest(final CreateNewOrderRequest request, final Product product) {
        return createOrderFromOrderRequest(ID, request, product);
    }

    public static Order createOrderFromOrderRequest(final UUID orderId, final CreateNewOrderRequest request, final Product product) {
        final var now = DateUtils.nowAsInstant();
        final List<OrderedProduct> products = CollectionUtils.isNotEmpty(request.getOrderedProducts())
                ? request.getOrderedProducts().stream()
                .map(p -> createProductFromRequest(p, product))
                .collect(Collectors.toList())
                : List.of();

        return Order.builder()
                .id(orderId)
                .name(request.getName())
                .isDeleted(product.isDeleted())
                .products(products)
                .priceSummary(Prices.calculatePriceSummary(products))
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    private static OrderedProduct createProductFromRequest(final CreateNewOrderedProductRequest orderedProduct, final Product product) {
        val result = OrderedProduct.builder()
                .id(orderedProduct.getProductId())
                .quantity(orderedProduct.getQuantity())
                .createdAt(product.getCreatedAt())
                .lastModifiedAt(product.getLastModifiedAt())
                .build();
        result.setPrice(product.getPrice());

        return result;
    }

    private static OrderedProduct createProductFromRequest(final UpdateOrderedProductRequest orderedProduct, final Product product) {
        return OrderedProduct.builder()
                .id(orderedProduct.getProductId())
                .quantity(orderedProduct.getQuantity())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .lastModifiedAt(product.getLastModifiedAt())
                .build();
    }

    public static CurrencyConversionResponse createCurrencyConversionResponse(final Product product) {
        val from = new CurrencyConversionValueDto();
        from.setCode(product.getPrice().get(0).getCurrency().getCode());
        from.setValue(product.getPrice().get(0).getValue().doubleValue());

        val to = new CurrencyConversionValueDto();
        to.setCode(DEFAULT_CURRENCY.getCode());
        to.setValue(product.getPrice().get(0).getValue().doubleValue());

        val response = new CurrencyConversionResponse();
        response.setId(product.getId().toString());
        response.setDate(LocalDate.now());
        response.setFrom(from);
        response.setTo(to);

        return response;
    }

    public static Product createProduct() {
        final var now = DateUtils.nowAsInstant();

        return Product.builder()
                .id(UUID.randomUUID())
                .price(new Prices(new Price(DEFAULT_CURRENCY, BigDecimal.valueOf(getRandomPriceValue()), now)))
                .details(PRODUCT_DETAILS)
                .isDeleted(false)
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static Product createProduct(CurrencyCode currency) {
        final var now = DateUtils.nowAsInstant();

        return Product.builder()
                .id(UUID.randomUUID())
                .price(new Prices(new Price(currency, BigDecimal.valueOf(getRandomPriceValue()), now)))
                .details(PRODUCT_DETAILS)
                .isDeleted(false)
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static UpdateOrderRequest updateOrderRequestEmpty() {
        return new UpdateOrderRequest();
    }

    public static UpdateOrderRequest updateOrderRequest(final String name, final Product updateProduct, final Double updateProductQuantity, final Product newProduct) {
        var request = new UpdateOrderRequest();
        request.setName(name);

        if (updateProduct != null) {
            var productToUpdate = new UpdateOrderedProductRequest();
            productToUpdate.setProductId(updateProduct.getId());
            productToUpdate.setQuantity(updateProductQuantity);

            request.setOrderedProducts(List.of(productToUpdate));
        }

        if (newProduct != null) {
            var newProductToAdd = new CreateNewOrderedProductRequest();
            newProductToAdd.setProductId(newProduct.getId());
            newProductToAdd.setQuantity(ORDER_QUANTITY);

            request.setNewProducts(List.of(newProductToAdd));
        }

        return request;
    }

    public static UpdateOrderRequest updateOrderRequest(final String name, final Product updateProduct, final Double updateProductQuantity, final Product newProduct, final boolean removeUpdatedProduct) {
        var request = new UpdateOrderRequest();
        request.setName(name);

        if (updateProduct != null) {
            var productToUpdate = new UpdateOrderedProductRequest();
            productToUpdate.setProductId(updateProduct.getId());
            productToUpdate.setQuantity(updateProductQuantity);

            request.setOrderedProducts(List.of(productToUpdate));

            if (removeUpdatedProduct) {
                request.setRemoveProducts(List.of(updateProduct.getId()));
            }
        }

        if (newProduct != null) {
            var newProductToAdd = new CreateNewOrderedProductRequest();
            newProductToAdd.setProductId(newProduct.getId());
            newProductToAdd.setQuantity(ORDER_QUANTITY);

            request.setNewProducts(List.of(newProductToAdd));
        }

        return request;
    }

    public static UpdateOrderRequest updateOrderRequest(final List<UUID> removeProducts) {
        var request = new UpdateOrderRequest();

        if (CollectionUtils.isNotEmpty(removeProducts)) {
            request.setRemoveProducts(removeProducts);
        }

        return request;
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final Product updatedProduct, final Product addedProduct) {
        final var now = DateUtils.nowAsInstant();
        var products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts())) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }
        return Order.builder()
                .id(ID)
                .name(request.getName())
                .isDeleted(false)
                .products(products)
                .priceSummary(Prices.calculatePriceSummary(products))
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder, final Product updatedProduct, final Product addedProduct) {
        final var now = DateUtils.nowAsInstant();
        var products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(previousOrder.getOrderedProducts())) {
            products.add(createProductFromRequest(previousOrder.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts())) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }
        return Order.builder()
                .id(ID)
                .name(previousOrder.getName() != null ? previousOrder.getName() : request.getName())
                .isDeleted(false)
                .products(products)
                .priceSummary(Prices.calculatePriceSummary(products))
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder) {
        final var now = DateUtils.nowAsInstant();
        var products = new ArrayList<OrderedProduct>();
        return Order.builder()
                .id(ID)
                .name(previousOrder.getName() != null ? previousOrder.getName() : request.getName())
                .isDeleted(false)
                .products(products)
                .priceSummary(Prices.calculatePriceSummary(products))
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder, final Product updatedProduct, final Product addedProduct, final boolean skipRequest) {
        final var now = DateUtils.nowAsInstant();
        var products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(previousOrder.getOrderedProducts())) {
            products.add(createProductFromRequest(previousOrder.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts()) && !skipRequest) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }
        return Order.builder()
                .id(ID)
                .name(previousOrder.getName() != null ? previousOrder.getName() : request.getName())
                .isDeleted(false)
                .products(products)
                .priceSummary(Prices.calculatePriceSummary(products))
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder, final Product updatedProduct, final Product addedProduct, final boolean skipRequest, final boolean removeUpdatedProduct) {
        final var now = DateUtils.nowAsInstant();
        var products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(previousOrder.getOrderedProducts())) {
            products.add(createProductFromRequest(previousOrder.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts()) && !skipRequest) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }
        if (Objects.nonNull(updatedProduct) && removeUpdatedProduct) {
            products.removeIf(toRemove -> toRemove.getId().equals(updatedProduct.getId()));
        }
        return Order.builder()
                .id(ID)
                .name(previousOrder.getName() != null ? previousOrder.getName() : request.getName())
                .isDeleted(false)
                .products(products)
                .priceSummary(Prices.calculatePriceSummary(products))
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    private static double getRandomPriceValue() {
        return randomDataGenerator.nextUniform(1, 100);
    }

}
