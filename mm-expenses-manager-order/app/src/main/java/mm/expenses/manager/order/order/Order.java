package mm.expenses.manager.order.order;

import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.utils.price.PriceSummary;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.price.OrderPrice;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emo_order")
@Builder(toBuilder = true)
@EntityListeners({
        AuditingEntityListener.class
})
public class Order implements Serializable, PriceSummary {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "name")
    @SpecificationDetailsAnnotation(canBeFiltered = true, canBeSorted = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", updatable = false)
    private List<OrderPrice> prices;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", updatable = false)
    private List<OrderedProduct> products;

    @Column(name = "created_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant createdAt;

    @Column(name = "last_modified_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant lastModifiedAt;

    @Column(name = "is_deleted")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private boolean isDeleted;

    @Version
    @Column(name = "version")
    private Long version;

    @Setter
    @Transient
    private Prices priceSummary;

    @Override
    public Prices getPriceSummary() {
        if (Objects.nonNull(products)) {
            priceSummary = Prices.calculatePriceSummary(products);
        }
        return priceSummary;
    }

    @PrePersist
    private void beforeSave() {
        val now = DateUtils.nowAsInstant();
        setCreatedAt(now);
        setLastModifiedAt(now);

        if (Objects.nonNull(products)) {
            products.forEach(product -> {
                product.setCreatedAt(now);
                product.setLastModifiedAt(now);
                product.setOrder(this);
            });
        }

        if (Objects.nonNull(prices)) {
            prices.forEach(price -> {
                price.setCreatedAt(now);
                price.setLastModifiedAt(now);
                price.setOrder(this);
                price.setDate(DateUtils.instantToLocalDate(now).toString());
            });
        }
    }

}
