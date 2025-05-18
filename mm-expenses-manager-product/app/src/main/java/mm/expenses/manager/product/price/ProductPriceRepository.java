package mm.expenses.manager.product.price;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID>, JpaSpecificationExecutor<ProductPrice> {

}
