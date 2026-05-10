package mm.expenses.manager.order.processor;

import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderPrice;
import mm.expenses.manager.order.core.OrderedProduct;
import mm.expenses.manager.order.core.OrderedProductPrice;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductPrice;
import mm.expenses.manager.order.product.ProductPrices;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.time.Instant;
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
        val request = new CreateNewOrderRequest();
        request.setName(name);
        request.setOrderedProducts(products);

        return request;
    }

    public static CreateNewOrderRequest createOrderRequest(final String name, final Product product) {
        return createOrderRequest(name, product, ORDER_QUANTITY);
    }

    public static CreateNewOrderRequest createOrderRequest(final String name, final Product product, final Double quantity) {
        val request = new CreateNewOrderRequest();
        request.setName(name);

        if (Objects.nonNull(product)) {
            val price = createPriceRequest(product);
            val newProduct = createNewOrderedProductRequest(product, quantity, price);

            request.setOrderedProducts(List.of(newProduct));
        }
        return request;
    }

    public static CreateNewOrderRequest createOrderRequestSkipQuantity(final String name, final Product product, final boolean skipQuantity) {
        return createOrderRequest(name, product, skipQuantity ? null : ORDER_QUANTITY);
    }

    public static CreateNewOrderRequest createOrderRequestEmptyProductId(final String name) {
        val newProduct = createNewOrderedProductRequest(null, ORDER_QUANTITY, null);
        val request = new CreateNewOrderRequest();
        request.setName(name);
        request.setOrderedProducts(List.of(newProduct));

        return request;
    }

    public static Order createOrderFromOrderRequest(final CreateNewOrderRequest request, final Product product) {
        return createOrderFromOrderRequest(ID, request, product);
    }

    public static Order createOrderFromOrderRequest(final CreateNewOrderRequest request, final Product product, final CurrencyCode currency) {
        return createOrderFromOrderRequest(ID, request, product, currency);
    }

    public static Order createOrderFromOrder(final Order order, final CurrencyCode currency) {
        val newOrder = (Order) SerializationUtils.clone(order);
        newOrder.getProducts().forEach(product -> {
            product.getPrices().forEach(price -> {
                price.setCurrency(currency);
            });
            product.getPriceSummary().forEach(price -> {
                price.setCurrency(currency);
            });
        });
        newOrder.getPriceSummary().forEach(price -> {
            price.setCurrency(currency);
        });
        return newOrder;
    }

    public static Order createOrderFromOrderRequest(final UUID orderId, final CreateNewOrderRequest request, final Product product, final CurrencyCode currency) {
        val now = DateUtils.nowAsInstant();
        final List<OrderedProduct> products = CollectionUtils.isNotEmpty(request.getOrderedProducts())
                ? request.getOrderedProducts().stream()
                .map(p -> createProductFromRequest(p, product, currency))
                .collect(Collectors.toList())
                : List.of();

        val summary = Prices.calculatePriceSummary(products, currency);
        val prices = new ArrayList<OrderPrice>();
        summary.forEach(price -> {
            val orderPrice = createOrderPrice(price, now);

            prices.add(orderPrice);
        });

        val order = createOrder(
                Order.builder()
                        .id(orderId)
                        .name(request.getName())
                        .isDeleted(product.isDeleted())
                        .products(products),
                prices,
                summary,
                now
        );

        prices.forEach(price -> {
            price.setOrder(order);
            price.setDeleted(order.isDeleted());
        });
        return order;
    }

    public static Order createOrderFromOrderRequest(final UUID orderId, final CreateNewOrderRequest request, final Product product) {
        val now = DateUtils.nowAsInstant();
        final List<OrderedProduct> products = CollectionUtils.isNotEmpty(request.getOrderedProducts())
                ? request.getOrderedProducts().stream()
                .map(p -> createProductFromRequest(p, product))
                .collect(Collectors.toList())
                : List.of();

        val summary = Prices.calculatePriceSummary(products);
        val prices = new ArrayList<OrderPrice>();
        summary.forEach(price -> {
            val orderPrice = createOrderPrice(price, now);

            prices.add(orderPrice);
        });

        val order = createOrder(
                Order.builder()
                        .id(orderId)
                        .name(request.getName())
                        .isDeleted(product.isDeleted())
                        .products(products),
                prices,
                summary,
                now
        );

        prices.forEach(price -> {
            price.setOrder(order);
            price.setDeleted(order.isDeleted());
        });
        return order;
    }

    public static CurrencyConversionResponse createCurrencyConversionResponse(final Product product) {
        val from = new CurrencyConversionValueDto();
        from.setCode(product.getPrices().get(0).getCurrency().getCode());
        from.setValue(product.getPrices().get(0).getValue().doubleValue());

        val to = new CurrencyConversionValueDto();
        to.setCode(DEFAULT_CURRENCY.getCode());
        to.setValue(product.getPrices().get(0).getValue().doubleValue());

        val response = new CurrencyConversionResponse();
        response.setId(product.getId().toString());
        response.setDate(LocalDate.now());
        response.setFrom(from);
        response.setTo(to);

        return response;
    }

    public static Product createProduct() {
        val now = DateUtils.nowAsInstant();

        return createProduct(DEFAULT_CURRENCY, now, false);
    }

    public static Product createProduct(final CurrencyCode currency) {
        val now = DateUtils.nowAsInstant();

        return createProduct(currency, now, false);
    }

    public static Product createProduct(final boolean isDeleted) {
        val now = DateUtils.nowAsInstant();

        return createProduct(DEFAULT_CURRENCY, now, isDeleted);
    }

    public static UpdateOrderRequest updateOrderRequestEmpty() {
        return new UpdateOrderRequest();
    }

    public static UpdateOrderRequest updateOrderRequest(final String name, final Product updateProduct, final Double updateProductQuantity, final Product newProduct) {
        val request = new UpdateOrderRequest();
        request.setName(name);

        if (updateProduct != null) {
            val price = createPriceRequest(updateProduct);
            val productToUpdate = new UpdateOrderedProductRequest();
            productToUpdate.setOrderedProductId(updateProduct.getId());
            productToUpdate.setQuantity(updateProductQuantity);
            productToUpdate.setPrice(price);

            request.setOrderedProducts(List.of(productToUpdate));
        }

        if (newProduct != null) {
            val price = createPriceRequest(newProduct);
            val newProductToAdd = createNewOrderedProductRequest(newProduct, ORDER_QUANTITY, price);

            request.setNewProducts(List.of(newProductToAdd));
        }

        return request;
    }

    public static UpdateOrderRequest updateOrderRequest(final List<UUID> removeProducts) {
        val request = new UpdateOrderRequest();
        if (CollectionUtils.isNotEmpty(removeProducts)) {
            request.setRemoveProducts(removeProducts);
        }
        return request;
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final Product updatedProduct, final Product addedProduct) {
        val now = DateUtils.nowAsInstant();
        val products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts())) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }

        val summary = Prices.calculatePriceSummary(products);
        val prices = new ArrayList<OrderPrice>();
        summary.forEach(price -> prices.add(createOrderPrice(price, now)));

        val order = createOrder(
                Order.builder()
                        .id(ID)
                        .name(request.getName())
                        .isDeleted(false)
                        .products(products),
                prices,
                summary,
                now
        );

        prices.forEach(price -> {
            price.setOrder(order);
            price.setDeleted(order.isDeleted());
        });
        return order;
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder, final Product updatedProduct, final Product addedProduct) {
        val now = DateUtils.nowAsInstant();
        val products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(previousOrder.getOrderedProducts())) {
            products.add(createProductFromRequest(previousOrder.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts())) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }

        val summary = Prices.calculatePriceSummary(products);
        val prices = new ArrayList<OrderPrice>();
        summary.forEach(price -> prices.add(createOrderPrice(price, now)));

        val order = createOrder(
                Order.builder()
                        .id(ID)
                        //.name(previousOrder.getName() != null ? previousOrder.getName() : request.getName())
                        .name(request.getName() != null ? request.getName() : previousOrder.getName())
                        .isDeleted(false)
                        .products(products),
                prices,
                summary,
                now
        );

        prices.forEach(price -> {
            price.setOrder(order);
            price.setDeleted(order.isDeleted());
        });
        return order;
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder) {
        val now = DateUtils.nowAsInstant();
        val products = new ArrayList<OrderedProduct>();
        return createOrder(request, previousOrder, products, now);
    }

    public static Order createOrderFromUpdateOrderRequest(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder, final Product updatedProduct, final Product addedProduct, final boolean skipRequest) {
        val now = DateUtils.nowAsInstant();
        val products = new ArrayList<OrderedProduct>();
        if (CollectionUtils.isNotEmpty(previousOrder.getOrderedProducts())) {
            products.add(createProductFromRequest(previousOrder.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getOrderedProducts()) && !skipRequest) {
            products.add(createProductFromRequest(request.getOrderedProducts().get(0), updatedProduct));
        }
        if (CollectionUtils.isNotEmpty(request.getNewProducts())) {
            products.add(createProductFromRequest(request.getNewProducts().get(0), addedProduct));
        }

        val summary = Prices.calculatePriceSummary(products);
        val prices = new ArrayList<OrderPrice>();
        summary.forEach(price -> prices.add(createOrderPrice(price, now)));

        val order = createOrder(
                Order.builder()
                        .id(ID)
                        .name(previousOrder.getName() != null ? previousOrder.getName() : request.getName())
                        .isDeleted(false)
                        .products(products),
                prices,
                summary,
                now
        );

        prices.forEach(price -> {
            price.setOrder(order);
            price.setDeleted(order.isDeleted());
        });
        return order;
    }

    private static double getRandomPriceValue() {
        return randomDataGenerator.nextUniform(1, 100);
    }

    private static Order createOrder(final Order.OrderBuilder ID, final ArrayList<OrderPrice> prices, final Prices summary, final Instant now) {
        return ID.prices(prices)
                .priceSummary(summary)
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    private static Order createOrder(final UpdateOrderRequest request, final CreateNewOrderRequest previousOrder, final ArrayList<OrderedProduct> products, final Instant now) {
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

    private static CreateNewOrderedProductRequest createNewOrderedProductRequest(final Product product, final Double quantity, final PriceRequest price) {
        val newProduct = new CreateNewOrderedProductRequest();
        newProduct.setQuantity(quantity);
        newProduct.setPrice(price);
        if (Objects.nonNull(product)) {
            newProduct.setProductId(product.getId());
        }
        return newProduct;
    }

    private static PriceRequest createPriceRequest(final Product product) {
        val price = new PriceRequest();
        price.setValue(product.getPrices().get(0).getValue());
        price.setCurrency(product.getPrices().get(0).getCurrency().getCode());
        return price;
    }

    private static OrderPrice createOrderPrice(final Price price, final Instant now) {
        val orderPrice = new OrderPrice();
        orderPrice.setValue(price.getValue());
        orderPrice.setCurrency(price.getCurrency());
        orderPrice.setCreatedAt(now);
        orderPrice.setLastModifiedAt(now);
        orderPrice.setDate(now.toString());
        orderPrice.setPriceOriginal(true);
        return orderPrice;
    }

    private static OrderedProduct createProductFromRequest(final CreateNewOrderedProductRequest orderedProduct, final Product product) {
        return createProductFromRequest(orderedProduct.getProductId(), orderedProduct.getQuantity(), orderedProduct.getPrice())
                .createdAt(product.getCreatedAt())
                .lastModifiedAt(product.getLastModifiedAt())
                .build();
    }

    private static OrderedProduct createProductFromRequest(final CreateNewOrderedProductRequest orderedProduct, final Product product, final CurrencyCode currency) {
        return createProductFromRequest(orderedProduct.getProductId(), orderedProduct.getQuantity(), orderedProduct.getPrice(), currency)
                .createdAt(product.getCreatedAt())
                .lastModifiedAt(product.getLastModifiedAt())
                .build();
    }

    private static OrderedProduct createProductFromRequest(final UpdateOrderedProductRequest orderedProduct, final Product product) {
        return createProductFromRequest(orderedProduct.getOrderedProductId(), orderedProduct.getQuantity(), orderedProduct.getPrice())
                .createdAt(product.getCreatedAt())
                .lastModifiedAt(product.getLastModifiedAt())
                .build();
    }

    private static OrderedProduct.OrderedProductBuilder createProductFromRequest(final UUID orderedProductId, final Double quantity, final PriceRequest priceRequest) {
        val price = OrderedProductPrice.builder()
                .value(priceRequest.getValue())
                .currency(CurrencyCode.valueOf(priceRequest.getCurrency()))
                .isPriceOriginal(true)
                .build();

        val prices = new ArrayList<OrderedProductPrice>();
        prices.add(price);

        return OrderedProduct.builder()
                .id(orderedProductId)
                .quantity(quantity)
                .prices(prices);
    }

    private static OrderedProduct.OrderedProductBuilder createProductFromRequest(final UUID orderedProductId, final Double quantity, final PriceRequest priceRequest, final CurrencyCode currency) {
        val price = OrderedProductPrice.builder()
                .value(priceRequest.getValue())
                .currency(currency)
                .isPriceOriginal(true)
                .build();

        val prices = new ArrayList<OrderedProductPrice>();
        prices.add(price);

        return OrderedProduct.builder()
                .id(orderedProductId)
                .quantity(quantity)
                .prices(prices);
    }

    private static Product createProduct(final CurrencyCode defaultCurrency, final Instant now, final boolean isDeleted) {
        final var productPrices = new ProductPrices();
        productPrices.add(new ProductPrice(defaultCurrency, BigDecimalWrapper.of(getRandomPriceValue()), DateUtils.instantToLocalDate(now).toString(), true));

        return Product.builder()
                .id(UUID.randomUUID())
                .prices(productPrices)
                .details(PRODUCT_DETAILS)
                .isDeleted(isDeleted)
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

}
