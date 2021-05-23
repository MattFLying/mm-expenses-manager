package mm.expenses.manager.product.product;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.dto.request.PriceRequest;
import mm.expenses.manager.product.product.dto.request.ProductRequest;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProductHelper {

    private static final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    public static final String ID = UUID.randomUUID().toString();
    public static final String PRODUCT_NAME = UUID.randomUUID().toString();
    public static final Map<String, Object> PRODUCT_DETAILS = Map.of("key", "value");

    public static ProductRequest createProductRequest(final String name, final BigDecimal priceValue, final CurrencyCode currency, final Map<String, Object> details) {
        return ProductRequest.builder()
                .name(name)
                .price(PriceRequest.builder().value(priceValue).currency(Objects.nonNull(currency) ? currency.getCode() : null).build())
                .details(details)
                .build();
    }

    public static ProductRequest createProductRequest(final String name, final BigDecimal priceValue, final CurrencyCode currency) {
        return createProductRequest(name, priceValue, currency, PRODUCT_DETAILS);
    }

    public static ProductRequest createProductRequest(final String name, final BigDecimal priceValue, final String currency) {
        return ProductRequest.builder()
                .name(name)
                .price(PriceRequest.builder().value(priceValue).currency(currency).build())
                .details(PRODUCT_DETAILS)
                .build();
    }

    public static ProductRequest createProductRequest(final String name, final CurrencyCode currency) {
        return createProductRequest(name, BigDecimal.valueOf(getRandomPriceValue()), currency);
    }

    public static ProductRequest createProductRequest(final String name, final CurrencyCode currency, final Map<String, Object> details) {
        return createProductRequest(name, BigDecimal.valueOf(getRandomPriceValue()), currency, details);
    }

    public static ProductRequest createProductRequestWithUnknownCurrency(final String name) {
        return createProductRequest(name, BigDecimal.valueOf(getRandomPriceValue()), "currency");
    }

    public static Product createProduct(final String name, final CurrencyCode currency, final Instant createdAndModifiedDate) {
        return createProduct(name, currency, BigDecimal.valueOf(getRandomPriceValue()), createdAndModifiedDate);
    }

    public static Product createProduct(final String name, final CurrencyCode currency, final BigDecimal price, final Instant createdAndModifiedDate) {
        return Product.builder()
                .id(ID)
                .name(name)
                .price(Price.builder().value(price).currency(currency).build())
                .details(PRODUCT_DETAILS)
                .createdAt(createdAndModifiedDate)
                .lastModifiedAt(createdAndModifiedDate)
                .version(1L)
                .build();
    }

    public static Product createProduct(final String name, final CurrencyCode currency) {
        return createProduct(name, currency, DateUtils.now());
    }

    public static Product createProduct(final String name, final BigDecimal price, final CurrencyCode currency) {
        return createProduct(name, currency, price, DateUtils.now());
    }

    public static Product createProductFromProductRequest(final ProductRequest productRequest) {
        final var now = DateUtils.now();
        return Product.builder()
                .id(ID)
                .name(productRequest.getName())
                .price(Price.builder().value(productRequest.getPrice().getValue()).currency(CurrencyCode.getCurrencyFromString(productRequest.getPrice().getCurrency(), true)).build())
                .details(productRequest.getDetails())
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    private static double getRandomPriceValue() {
        return randomDataGenerator.nextUniform(1, 100);
    }


}
