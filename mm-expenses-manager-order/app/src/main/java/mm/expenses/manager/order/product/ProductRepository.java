package mm.expenses.manager.order.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
interface ProductRepository extends MongoRepository<ProductEntity, String> {

    @Query("{ 'name': {$regex: ?0, $options: 'i'} }")
    Page<ProductEntity> findByName(final String name, final Pageable pageable);

    Page<ProductEntity> findByPrice_amountBetween(final double min, final double max, final Pageable pageable);

    Page<ProductEntity> findByPrice_amountGreaterThan(final double price, final Pageable pageable);

    Page<ProductEntity> findByPrice_amountLessThan(final double price, final Pageable pageable);

    List<ProductEntity> findByIdIn(final Set<String> ids);

    Long deleteByIdIn(final Set<String> ids);

}
