package mm.expenses.manager.product.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.product.processor.search.ProductFilter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emp_product_filter_view")
@Builder(toBuilder = true)
@EntityListeners({
        AuditingEntityListener.class
})
public class ProductFilterView implements Serializable {

    public static final String PRICE_VALUE_FIELD_NAME = "priceValue";
    public static final String PRICE_CURRENCY_FIELD_NAME = "priceCurrency";
    public static final String IS_PRICE_ORIGINAL_FIELD_NAME = "isPriceOriginal";

    @Id
    @Column(name = "product_id")
    @SpecificationDetailsAnnotation(canBeFiltered = true, canBeSorted = true)
    private UUID productId;

    @Column(name = "name")
    @SpecificationDetailsAnnotation(canBeFiltered = true, canBeSorted = true)
    private String name;

    @Column(name = "product_created_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant createdAt;

    @Column(name = "product_last_modified_at")
    private Instant lastModifiedAt;

    @Column(name = "is_product_deleted")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private boolean isProductDeleted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_details", columnDefinition = SpecificationScanner.JSONB_TYPE)
    private Map<String, Object> details;

    @Column(name = "price_id")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private UUID priceId;

    @Column(name = "price_value")
    @SpecificationDetailsAnnotation(name = ProductFilter.PRICE_VALUE_PROPERTY, canBeFiltered = true, canBeSorted = true)
    private BigDecimal priceValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_currency")
    @SpecificationDetailsAnnotation(name = ProductFilter.PRICE_CURRENCY_PROPERTY, canBeFiltered = true)
    private CurrencyCode priceCurrency;

    @Column(name = "is_price_original")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private boolean isPriceOriginal;

    @Column(name = "price_date")
    private String priceDate;

    @Column(name = "price_created_at")
    private Instant priceCreatedAt;

    @Column(name = "price_last_modified_at")
    private Instant priceLastModifiedAt;

}
