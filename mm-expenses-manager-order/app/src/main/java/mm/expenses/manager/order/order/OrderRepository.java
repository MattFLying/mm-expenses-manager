package mm.expenses.manager.order.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdAndIsDeleted(final UUID id, final boolean isDeleted);

    List<Order> findAllByIdInAndIsDeleted(final Set<UUID> ids, final boolean isDeleted);

}
