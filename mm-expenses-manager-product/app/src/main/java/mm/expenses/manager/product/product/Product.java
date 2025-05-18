package mm.expenses.manager.product.product;

import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.price.ProductPrice;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emp_product")
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
    @SpecificationDetailsAnnotation(canBeFiltered = true, canBeSorted = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", insertable = true, updatable = true)
    private List<ProductPrice> prices;

    @Column(name = "created_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant createdAt;

    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = SpecificationScanner.JSONB_TYPE)
    private Map<String, Object> details;

    @Column(name = "is_deleted")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private boolean isDeleted;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    private void beforeSave() {
        val now = DateUtils.nowAsInstant();
        if (Objects.nonNull(prices)) {
            prices.forEach(price -> {
                price.setCreatedAt(now);
                price.setLastModifiedAt(now);
            });
        }
        setCreatedAt(now);
        setLastModifiedAt(createdAt);
    }

    public void setPrice(List<ProductPrice> prices) {
        if (CollectionUtils.isNotEmpty(prices)) {
            this.prices = prices;
            this.prices.forEach(price -> {
                price.setProduct(this);
            });
        }
    }

}
