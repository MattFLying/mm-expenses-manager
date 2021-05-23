package mm.expenses.manager.product.product;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mm.expenses.manager.product.pageable.PageFactory;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.command.CreateProductCommand;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static mm.expenses.manager.product.product.ProductContext.createNewProduct;
import static mm.expenses.manager.product.product.ProductContext.findAll;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "name_idx", def = "{'name': 1}")
})
public class Product {

    @Id
    private final String id;

    private final String name;

    private final Price price;

    private final Instant createdAt;

    private final Instant lastModifiedAt;

    private Map<String, Object> details;

    @Version
    private final Long version;

    public static Product create(final CreateProductCommand createProductCommand) {
        return createNewProduct(createProductCommand);
    }

    public static Page<Product> findProducts(final ProductQueryFilter queryFilter, final Integer pageNumber, final Integer pageSize, final SortOrder sortOrder, final Boolean shouldSortDesc) {
        final var sortingOrders = getOrDefault(sortOrder).withDirectionAscOrDesc(shouldSortDesc).getOrders();
        final var pageable = PageFactory.getPageRequest(pageNumber, pageSize, Sort.by(sortingOrders));

        return findAll(queryFilter, pageable);
    }

    private static SortOrder getOrDefault(final SortOrder sortOrder) {
        return Optional.ofNullable(sortOrder).orElse(SortOrder.NAME);
    }

}
