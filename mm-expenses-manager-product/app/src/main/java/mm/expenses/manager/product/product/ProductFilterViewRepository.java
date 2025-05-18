package mm.expenses.manager.product.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface ProductFilterViewRepository extends JpaRepository<ProductFilterView, UUID>, JpaSpecificationExecutor<ProductFilterView> {

}
