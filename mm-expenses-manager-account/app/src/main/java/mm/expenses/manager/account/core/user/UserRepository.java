package mm.expenses.manager.account.core.user;

import mm.expenses.manager.common.utils.security.UserSimpleInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User>, UserSimpleInterface<User> {

    @Query(
            value = "SELECT u.* FROM em_account.user u WHERE (u.id = ?1 OR u.keycloak_id = ?2) AND u.is_deleted = ?3",
            nativeQuery = true
    )
    Optional<User> findByIdOrKeycloakIdAndIsDeleted(final UUID id, final UUID keycloakId, final boolean isDeleted);

    Optional<User> findByKeycloakId(final UUID keycloakId);

}
