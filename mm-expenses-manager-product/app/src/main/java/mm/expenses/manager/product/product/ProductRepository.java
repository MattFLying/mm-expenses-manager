package mm.expenses.manager.product.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByNameAndIsDeletedFalse(final String name, final Pageable pageable);

    Page<Product> findByNameAndPrice_valueAndIsDeletedFalse(final String name, final BigDecimal value, final Pageable pageable);

    Page<Product> findByNameAndPrice_valueLessThanAndIsDeletedFalse(final String name, final BigDecimal value, final Pageable pageable);

    Page<Product> findByNameAndPrice_valueGreaterThanAndIsDeletedFalse(final String name, final BigDecimal value, final Pageable pageable);

    Page<Product> findByNameAndPrice_valueBetweenAndIsDeletedFalse(final String name, final BigDecimal min, final BigDecimal max, final Pageable pageable);

    Page<Product> findByPrice_valueAndIsDeletedFalse(final BigDecimal value, final Pageable pageable);

    Page<Product> findByPrice_valueBetweenAndIsDeletedFalse(final BigDecimal min, final BigDecimal max, final Pageable pageable);

    Page<Product> findByPrice_valueGreaterThanAndIsDeletedFalse(final BigDecimal price, final Pageable pageable);

    Page<Product> findByPrice_valueLessThanAndIsDeletedFalse(final BigDecimal price, final Pageable pageable);

    Page<Product> findAllByIsDeletedFalse(final Pageable pageable);

    Page<Product> findAllByIsDeletedTrue(final Pageable pageable);

    void deleteByIdIn(final Collection<UUID> ids);





    Optional<Product> findByIdAndIsDeleted(UUID id, boolean isDeleted);





}
