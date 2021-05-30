package mm.expenses.manager.product.product;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.dto.request.CreatePriceRequest;
import mm.expenses.manager.product.product.dto.request.CreateProductRequest;
import mm.expenses.manager.product.product.dto.request.UpdatePriceRequest;
import mm.expenses.manager.product.product.dto.request.UpdateProductRequest;
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
    public static final CurrencyCode DEFAULT_CURRENCY = CurrencyCode.PLN;
    public static final Map<String, Object> PRODUCT_DETAILS = Map.of("key", "value");

    public static UpdateProductRequest updateProductRequest(final String name, final BigDecimal priceValue, final CurrencyCode currency, final Map<String, Object> details) {
        return UpdateProductRequest.builder()
                .name(name)
                .price(UpdatePriceRequest.builder().value(priceValue).currency(Objects.nonNull(currency) ? currency.getCode() : null).build())
                .details(details)
                .build();
    }

    public static UpdateProductRequest updateProductRequest(final String name) {
        return updateProductRequest(name, BigDecimal.valueOf(getRandomPriceValue()), DEFAULT_CURRENCY, PRODUCT_DETAILS);
    }

    public static UpdateProductRequest updateProductRequest(final BigDecimal value, final boolean valueIsNull) {
        return updateProductRequest(PRODUCT_NAME, valueIsNull ? null : value, DEFAULT_CURRENCY, PRODUCT_DETAILS);
    }

    public static UpdateProductRequest updateProductRequest(final CurrencyCode currency, final boolean currencyIsNull) {
        return updateProductRequest(PRODUCT_NAME, BigDecimal.valueOf(getRandomPriceValue()), currencyIsNull ? null : currency, PRODUCT_DETAILS);
    }

    public static UpdateProductRequest updateProductRequest(final boolean detailsIsNull, final Map<String, Object> details) {
        return updateProductRequest(PRODUCT_NAME, BigDecimal.valueOf(getRandomPriceValue()), DEFAULT_CURRENCY, detailsIsNull ? null : details);
    }

    public static CreateProductRequest createProductRequest(final String name, final BigDecimal priceValue, final CurrencyCode currency, final Map<String, Object> details) {
        return CreateProductRequest.builder()
                .name(name)
                .price(CreatePriceRequest.builder().value(priceValue).currency(Objects.nonNull(currency) ? currency.getCode() : null).build())
                .details(details)
                .build();
    }

    public static CreateProductRequest createProductRequest(final String name, final BigDecimal priceValue, final CurrencyCode currency) {
        return createProductRequest(name, priceValue, currency, PRODUCT_DETAILS);
    }

    public static CreateProductRequest createProductRequest(final String name, final BigDecimal priceValue, final String currency) {
        return CreateProductRequest.builder()
                .name(name)
                .price(CreatePriceRequest.builder().value(priceValue).currency(currency).build())
                .details(PRODUCT_DETAILS)
                .build();
    }

    public static CreateProductRequest createProductRequest(final String name, final CurrencyCode currency) {
        return createProductRequest(name, BigDecimal.valueOf(getRandomPriceValue()), currency);
    }

    public static CreateProductRequest createProductRequest(final String name, final CurrencyCode currency, final Map<String, Object> details) {
        return createProductRequest(name, BigDecimal.valueOf(getRandomPriceValue()), currency, details);
    }

    public static CreateProductRequest createProductRequestWithUnknownCurrency(final String name) {
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
                .isDeleted(false)
                .build();
    }

    public static Product createProduct() {
        return createProduct(PRODUCT_NAME, DEFAULT_CURRENCY, BigDecimal.valueOf(getRandomPriceValue()), DateUtils.now());
    }

    public static Product createProduct(final String name, final CurrencyCode currency) {
        return createProduct(name, currency, DateUtils.now());
    }

    public static Product createProduct(final String name, final BigDecimal price, final CurrencyCode currency) {
        return createProduct(name, currency, price, DateUtils.now());
    }

    public static Product createProductFromProductRequest(final CreateProductRequest createProductRequest) {
        final var now = DateUtils.now();
        return Product.builder()
                .id(ID)
                .name(createProductRequest.getName())
                .price(Price.builder().value(createProductRequest.getPrice().getValue()).currency(CurrencyCode.getCurrencyFromString(createProductRequest.getPrice().getCurrency(), true)).build())
                .details(createProductRequest.getDetails())
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    public static Product createProductFromUpdateProductRequest(final UpdateProductRequest updateProductRequest) {
        final var now = DateUtils.now();
        return Product.builder()
                .id(ID)
                .name(updateProductRequest.getName())
                .price(Price.builder().value(updateProductRequest.getPrice().getValue()).currency(CurrencyCode.getCurrencyFromString(updateProductRequest.getPrice().getCurrency(), true)).build())
                .details(updateProductRequest.getDetails())
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

    private static double getRandomPriceValue() {
        return randomDataGenerator.nextUniform(1, 100);
    }


}
