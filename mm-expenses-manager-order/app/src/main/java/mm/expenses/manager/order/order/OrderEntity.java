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

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

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
class OrderEntity implements Serializable {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "products", columnDefinition = "jsonb")
    private List<OrderedProductEntity> products;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_summary", columnDefinition = "jsonb")
    private Price priceSummary;

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

    @Data
    @Builder(toBuilder = true)
    static class OrderedProductEntity {

        private String id;

        private Instant createdAt;

        private Instant lastModifiedAt;

        private Double quantity;

        private BoughtProduct boughtProduct;

        private Price priceSummary;

        @Data
        @Builder(toBuilder = true)
        static class BoughtProduct {

            private String productId;

            private String name;

            private Price price;

        }

    }

}
