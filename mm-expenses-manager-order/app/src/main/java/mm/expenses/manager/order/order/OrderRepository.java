package mm.expenses.manager.order.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
interface OrderRepository extends JpaRepository<OrderEntity, String> {

    Long deleteByIdIn(final Set<String> ids);

}
