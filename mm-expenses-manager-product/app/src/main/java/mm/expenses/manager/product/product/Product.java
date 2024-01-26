package mm.expenses.manager.product.product;

import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.price.Price;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Version;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_product")
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

}
