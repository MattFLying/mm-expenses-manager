package mm.expenses.manager.order.core;

import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.PriceSummary;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order")
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

    @Override
    public Price getPriceSummary(final CurrencyCode currency) {
        if (Objects.nonNull(products)) {
            final var pricesForCurrency = products.stream()
                    .map(orderedProduct -> orderedProduct.getPriceSummary(currency))
                    .toList();

            priceSummary = Prices.ofCurrency(pricesForCurrency, currency);
        }
        return Optional.ofNullable(priceSummary)
                .flatMap(prices -> prices.getByCurrency(currency))
                .orElse(null);
    }

    @PrePersist
    private void beforeSave() {
        if (Objects.nonNull(products)) {
            products.forEach(product -> {
                product.setOrder(this);
            });
        }
        if (Objects.nonNull(prices)) {
            prices.forEach(price -> {
                price.setOrder(this);
            });
        }
    }

}
