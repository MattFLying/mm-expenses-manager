package mm.expenses.manager.product.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndIsDeleted(final UUID id, final boolean isDeleted);

    Page<Product> findAllByIsDeletedTrue(final Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN ProductPrice pp ON p.id = pp.product.id GROUP BY p.id HAVING COUNT(pp.product.id) < ?1")
    Page<Product> findAllWithCurrenciesLessThan(final int numberOfExpectedCurrencies, final Pageable pageable);

    void deleteByIdIn(final Collection<UUID> ids);

    List<Product> findAllByIdInAndIsDeleted(final Set<UUID> ids, final boolean isDeleted);

}
