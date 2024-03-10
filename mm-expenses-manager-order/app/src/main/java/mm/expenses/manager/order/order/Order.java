package mm.expenses.manager.order.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.currency.Price;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "o_order")
@Builder(toBuilder = true)
@EntityListeners({
        AuditingEntityListener.class
})
public class Order implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "products", columnDefinition = "jsonb")
    private List<OrderedProduct> products;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_summary", columnDefinition = "jsonb")
    private Price priceSummary;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Version
    @Column(name = "version")
    private Long version;

    public static Price calculatePriceSummary(final Collection<OrderedProduct> products) {
        return CollectionUtils.isEmpty(products)
                ? Price.empty()
                : products.stream().findFirst()
                .map(firstForCurrency -> {
                    final var currency = firstForCurrency.getPriceSummary().getCurrency();
                    return products.stream()
                            .map(OrderedProduct::getPriceSummary)
                            .reduce(new Price(currency, BigDecimal.ZERO), Order::accumulatePrices);
                })
                .orElse(Price.empty());
    }

    private static Price accumulatePrices(final Price first, final Price second) {
        return Price.add(first, second);
    }

    @PreUpdate
    private void beforeUpdate() {
        setLastModifiedAt(DateUtils.nowAsInstant());
    }

    @PrePersist
    private void beforeSave() {
        setCreatedAt(DateUtils.nowAsInstant());
    }

}
