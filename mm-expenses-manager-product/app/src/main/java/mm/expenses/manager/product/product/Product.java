package mm.expenses.manager.product.product;

import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.async.message.ProductMessage.Operation;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.exception.ProductNotFoundException;
import mm.expenses.manager.product.exception.ProductValidationException;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.command.CreateProductCommand;
import mm.expenses.manager.product.product.command.UpdateProductCommand;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.apache.commons.collections4.MapUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
@Builder(toBuilder = true)
@EntityListeners({
        AuditingEntityListener.class
})
public class Product implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "name")
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price", columnDefinition = "jsonb")
    private Price price;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Version
    @Column(name = "version")
    private Long version;

    @PreUpdate
    private void beforeUpdate() {
        setLastModifiedAt(DateUtils.nowAsInstant());
    }

    @PrePersist
    private void beforeSave() {
        setCreatedAt(DateUtils.nowAsInstant());
    }

    void delete() {
        setDeleted(true);
        ProductContext.saveProduct(this, Operation.DELETE);
    }

    void updateName(final String newName) {
        if (ProductCommonValidation.isNameEmpty(newName)) {
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
        if (MapUtils.isNotEmpty(newDetails)) {
            setDetails(newDetails);
        }
    }

    Product partiallyUpdate(final UpdateProductCommand updateProductCommand) {
        updateProductCommand.getName().ifPresent(this::updateName);
        updateProductCommand.getPrice().ifPresent(this::updatePrice);
        updateProductCommand.getDetails().ifPresent(this::updateDetails);

        return this;
    }

    public static Product create(final CreateProductCommand createProductCommand) {
        final var newProduct = Product.builder()
                .name(createProductCommand.getName())
                .price(createProductCommand.getPrice())
                .details(createProductCommand.getDetails())
                .build();

        return ProductContext.saveProduct(newProduct, Operation.CREATE);
    }

    public static Product update(final UpdateProductCommand updateProductCommand) {
        final var productId = updateProductCommand.getId();
        final var existed = ProductContext.findProductById(UUID.fromString(productId)).orElseThrow(() -> new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));

        return ProductContext.saveProduct(existed.partiallyUpdate(updateProductCommand), Operation.UPDATE);
    }

    public static Page<Product> findProducts(ProductQueryFilter queryFilter, PageRequest pageable) {
        return ProductContext.findAll(queryFilter, pageable);
    }

    public static Product findById(final UUID productId) {
        return ProductContext.findProductById(productId).orElseThrow(() -> new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));
    }

    public static void delete(final UUID productId) {
        ProductContext.findProductById(productId)
                .ifPresentOrElse(
                        Product::delete,
                        () -> {
                            throw new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId));
                        });
    }

}
