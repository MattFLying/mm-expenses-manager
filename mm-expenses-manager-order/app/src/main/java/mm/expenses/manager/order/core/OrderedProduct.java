package mm.expenses.manager.order.core;

import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.PriceSummary;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.order.product.Product;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_product")
@Builder(toBuilder = true)
@EntityListeners({
        AuditingEntityListener.class
})
public class OrderedProduct implements Serializable, PriceSummary {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "order_product_price",
            joinColumns = @JoinColumn(name = "order_product_id", referencedColumnName = "id", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    private List<OrderedProductPrice> prices = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", updatable = false)
    private Product product;

    @Column(name = "created_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant createdAt;

    @Column(name = "last_modified_at")
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
        if (Objects.nonNull(prices) && !prices.isEmpty()) {
            return getPrices().stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findAny()
                    .map(price -> new Prices(Price.multiply(price.getCurrency(), price.getValue(), quantity, createdAt)))
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Price getPriceSummary(final CurrencyCode currency) {
        if (Objects.nonNull(prices) && !prices.isEmpty()) {
            final var priceOfCurrency = getPrices().stream()
                    .filter(price -> Objects.equals(price.getCurrency(), currency))
                    .toList();

            OrderedProductPrice price = null;
            if (priceOfCurrency.size() == 1) {
                price = priceOfCurrency.get(0);
            } else if (priceOfCurrency.size() > 1) {
                final var priceOriginal = priceOfCurrency.stream()
                        .filter(OrderedProductPrice::isPriceOriginal)
                        .findAny();
                if (priceOriginal.isPresent()) {
                    price = priceOriginal.get();
                }
            }

            if (Objects.nonNull(price)) {
                return Price.multiply(price.getCurrency(), price.getValue(), quantity, createdAt);
            }
        }
        return null;
    }

    public void setPrices(final List<OrderedProductPrice> prices) {
        if (Objects.isNull(this.prices)) {
            this.prices = new ArrayList<>();
        }
        this.prices.clear();
        this.prices.addAll(prices);
    }

    public void addPrice(final OrderedProductPrice price) {
        if (Objects.isNull(this.prices)) {
            this.prices = new ArrayList<>();
        }
        this.prices.add(price);
    }

}
