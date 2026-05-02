package mm.expenses.manager.product.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductFilterViewRepository extends JpaRepository<ProductFilterView, UUID>, JpaSpecificationExecutor<ProductFilterView> {

}
