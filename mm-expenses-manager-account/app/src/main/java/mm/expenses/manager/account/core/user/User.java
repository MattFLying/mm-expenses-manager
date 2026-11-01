package mm.expenses.manager.account.core.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import mm.expenses.manager.common.utils.security.UserPermissions;
import mm.expenses.manager.common.utils.security.UserSimpleInterface;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.common.utils.util.DateUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
@Table(name = "user")
@Builder(toBuilder = true)
@EntityListeners({
        AuditingEntityListener.class
})
public class User extends UserSimpleInterface.SimpleUser implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "keycloak_id")
    private UUID keycloakId;

    @Column(name = "username")
    @SpecificationDetailsAnnotation(canBeFiltered = true, canBeSorted = true)
    private String username;

    @Column(name = "email")
    @SpecificationDetailsAnnotation(canBeFiltered = true, canBeSorted = true)
    private String email;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = SpecificationScanner.JSONB_TYPE)
    private Map<String, Object> details;

    @Column(name = "created_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant createdAt;

    @Column(name = "last_modified_at")
    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Instant lastModifiedAt;

    @Column(name = "is_deleted")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private boolean isDeleted;

    @JsonProperty(value = "is_email_verified")
    private boolean isEmailVerified;

    @JsonProperty(value = "is_enabled")
    private boolean isEnabled;

    @Version
    @Column(name = "version")
    private Long version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = SpecificationScanner.JSONB_TYPE)
    private UserPermissions permissions;

    @PrePersist
    private void beforeSave() {
        if (Objects.isNull(createdAt)) {
            setCreatedAt(DateUtils.nowAsInstant());
        }
        if (Objects.isNull(lastModifiedAt)) {
            setLastModifiedAt(getCreatedAt());
        }
    }

}
