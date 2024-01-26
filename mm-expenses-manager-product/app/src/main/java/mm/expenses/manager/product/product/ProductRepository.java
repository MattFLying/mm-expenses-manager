package mm.expenses.manager.product.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndIsDeleted(final UUID id, final boolean isDeleted);

    Page<Product> findAllByIsDeletedTrue(final Pageable pageable);

    void deleteByIdIn(final Collection<UUID> ids);

    @Query(value = "SELECT p.* FROM p_product p WHERE p.is_deleted = false", nativeQuery = true)
    Page<Product> findAllNotDeleted(final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE cast(p.price -> 'value' as float) BETWEEN :min AND :max AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByPriceBetweenAndNotDeleted(@Param("min") final BigDecimal min, @Param("max") final BigDecimal max, final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE p.name LIKE %:name% AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByNameAndNotDeleted(@Param("name") final String name, final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE cast(p.price -> 'value' as float) < :price AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByPriceLessThanAndNotDeleted(@Param("price") final BigDecimal price, final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE cast(p.price -> 'value' as float) > :price AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByPriceGreaterThanAndNotDeleted(@Param("price") final BigDecimal price, final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE p.name LIKE %:name% AND cast(p.price -> 'value' as float) < :price AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByNameAndPriceLessThanAndNotDeleted(@Param("name") final String name, @Param("price") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE p.name LIKE %:name% AND cast(p.price -> 'value' as float) > :price AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByNameAndPriceGreaterThanAndNotDeleted(@Param("name") final String name, @Param("price") final BigDecimal value, final Pageable pageable);

    @Query(value = "SELECT p.* FROM p_product p WHERE p.name LIKE %:name% AND cast(p.price -> 'value' as float) BETWEEN :min AND :max AND p.is_deleted = false", nativeQuery = true)
    Page<Product> findByNameAndPriceBetweenAndNotDeleted(@Param("name") final String name, @Param("min") final BigDecimal min, @Param("max") final BigDecimal max, final Pageable pageable);

}
