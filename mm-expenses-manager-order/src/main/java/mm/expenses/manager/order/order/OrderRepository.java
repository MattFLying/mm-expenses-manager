package mm.expenses.manager.order.order;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
interface OrderRepository extends MongoRepository<OrderEntity, String> {

    Long deleteByIdIn(final Set<String> ids);

}
