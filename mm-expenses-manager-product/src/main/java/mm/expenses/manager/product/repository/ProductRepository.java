package mm.expenses.manager.product.repository;

import mm.expenses.manager.product.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    @Query("{ 'name': {$regex: ?0, $options: 'i'}, 'isDeleted': false }")
    Page<Product> findByName(final String name, final Pageable pageable);

    @Query("{ 'name': {$regex: ?0, $options: 'i'}, 'price': ?1, 'isDeleted': false }")
    Page<Product> findByNameAndPrice_value(final String name, final BigDecimal value, final Pageable pageable);

    @Query("{ 'name': {$regex: ?0, $options: 'i'}, 'price': {$lte: ?1}, 'isDeleted': false }")
    Page<Product> findByNameAndPrice_valueLessThan(final String name, final BigDecimal value, final Pageable pageable);

    @Query("{ 'name': {$regex: ?0, $options: 'i'}, 'price': {$gte: ?1}, 'isDeleted': false }")
    Page<Product> findByNameAndPrice_valueGreaterThan(final String name, final BigDecimal value, final Pageable pageable);

    @Query("{ 'name': {$regex: ?0, $options: 'i'}, $and:[ {'price':{$gte:?1}}, {'price':{$lte:?2}} ] }, 'isDeleted': false }")
    Page<Product> findByNameAndPrice_valueBetween(final String name, final BigDecimal min, final BigDecimal max, final Pageable pageable);

    Page<Product> findByPrice_valueAndIsDeletedFalse(final BigDecimal value, final Pageable pageable);

    Page<Product> findByPrice_valueBetweenAndIsDeletedFalse(final BigDecimal min, final BigDecimal max, final Pageable pageable);

    Page<Product> findByPrice_valueGreaterThanAndIsDeletedFalse(final BigDecimal price, final Pageable pageable);

    Page<Product> findByPrice_valueLessThanAndIsDeletedFalse(final BigDecimal price, final Pageable pageable);

    Page<Product> findAllByIsDeletedFalse(final Pageable pageable);

    Page<Product> findAllByIsDeletedTrue(final Pageable pageable);

    void deleteByIdIn(final Collection<String> ids);

}
