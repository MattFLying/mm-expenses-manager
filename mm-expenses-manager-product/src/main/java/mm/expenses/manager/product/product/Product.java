package mm.expenses.manager.product.product;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.exception.ProductNotFoundException;
import mm.expenses.manager.product.exception.ProductValidationException;
import mm.expenses.manager.product.pageable.PageFactory;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.command.CreateProductCommand;
import mm.expenses.manager.product.product.command.UpdateProductCommand;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import mm.expenses.manager.product.product.validator.ProductValidator;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static mm.expenses.manager.product.product.ProductContext.findAll;
import static mm.expenses.manager.product.product.ProductContext.findProductById;
import static mm.expenses.manager.product.product.ProductContext.saveProduct;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "name_idx", def = "{'name': 1}")
})
public class Product {

    @Id
    private final String id;

    private String name;

    private Price price;

    private Instant createdAt;

    private Instant lastModifiedAt;

    private Map<String, Object> details;

    private boolean isDeleted;

    @Version
    private final Long version;

    void delete() {
        final var now = DateUtils.now();

        setDeleted(true);
        setLastModifiedAt(now);

        saveProduct(this);
    }

    Product partiallyUpdate(final UpdateProductCommand updateProductCommand) {
        updateProductCommand.getName().ifPresent(this::updateName);
        updateProductCommand.getPrice().ifPresent(this::updatePrice);
        updateProductCommand.getDetails().ifPresent(this::updateDetails);

        final var now = DateUtils.now();
        setLastModifiedAt(now);

        return this;
    }

    void updateName(final String newName) {
        if (!ProductValidator.isProductNameValid(newName)) {
            throw new ProductValidationException(ProductExceptionMessage.PRODUCT_NAME_NOT_VALID.withParameters(newName));
        }
        setName(newName);
    }

    void updatePrice(final Price newPrice) {
        var priceBuilder = Price.builder();

        if (Objects.nonNull(newPrice.getOriginalValue())) {
            if (!newPrice.isValueValid()) {
                throw new ProductValidationException(ProductExceptionMessage.PRODUCT_PRICE_VALUE_NOT_VALID.withParameters(newPrice.getValue()));
            }
            priceBuilder.value(newPrice.getValue());
        } else {
            priceBuilder.value(getPrice().getValue());
        }

        if (Objects.nonNull(newPrice.getOriginalCurrency())) {
            if (!newPrice.isCurrencyCodeValid()) {
                throw new ProductValidationException(ProductExceptionMessage.PRODUCT_PRICE_CURRENCY_NOT_VALID);
            }
            priceBuilder.currency(newPrice.getCurrency());
        } else {
            priceBuilder.currency(getPrice().getCurrency());
        }

        setPrice(priceBuilder.build());
    }

    void updateDetails(final Map<String, Object> newDetails) {
        if (!ProductValidator.isProductDetailsValid(newDetails)) {
            throw new ProductValidationException(ProductExceptionMessage.PRODUCT_DETAILS_NOT_VALID.withParameters(newDetails));
        }
        setDetails(newDetails);
    }

    public static Product create(final CreateProductCommand createProductCommand) {
        final var now = DateUtils.now();
        final var newProduct = Product.builder()
                .name(createProductCommand.getName())
                .price(createProductCommand.getPrice())
                .details(createProductCommand.getDetails())
                .createdAt(now)
                .lastModifiedAt(now)
                .build();

        return saveProduct(newProduct);
    }

    public static Product update(final UpdateProductCommand updateProductCommand) {
        final var productId = updateProductCommand.getId();
        final var existed = findProductById(productId).orElseThrow(() -> new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));

        return saveProduct(existed.partiallyUpdate(updateProductCommand));
    }

    public static Page<Product> findProducts(final ProductQueryFilter queryFilter, final Integer pageNumber, final Integer pageSize, final SortOrder sortOrder, final Boolean shouldSortDesc) {
        final var sortingOrders = getOrDefault(sortOrder).withDirectionAscOrDesc(shouldSortDesc).getOrders();
        final var pageable = PageFactory.getPageRequest(pageNumber, pageSize, Sort.by(sortingOrders));

        return findAll(queryFilter, pageable);
    }

    public static Product findById(final String productId) {
        return findProductById(productId).orElseThrow(() -> new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));
    }

    public static void delete(final String productId) {
        findProductById(productId)
                .ifPresentOrElse(
                        Product::delete,
                        () -> {
                            throw new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId));
                        });
    }

    private static SortOrder getOrDefault(final SortOrder sortOrder) {
        return Optional.ofNullable(sortOrder).orElse(SortOrder.NAME);
    }

}
