package mm.expenses.manager.product.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndIsDeleted(final UUID id, final boolean isDeleted);

    Page<Product> findAllByIsDeletedTrue(final Pageable pageable);

    void deleteByIdIn(final Collection<UUID> ids);

}
