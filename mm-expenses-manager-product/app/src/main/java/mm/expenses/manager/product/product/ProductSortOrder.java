package mm.expenses.manager.product.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortOrder;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortProperty;
import mm.expenses.manager.common.postgresql.pagination.sort.SortJsonBProperty;
import mm.expenses.manager.common.utils.pagination.sort.SortProperty;
import mm.expenses.manager.product.api.product.model.SortProductRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
enum ProductSortOrder implements PostgreSQLSortOrder {
    NAME_ASC(List.of(new PostgreSQLSortProperty("name", Direction.ASC))),
    NAME_DESC(List.of(new PostgreSQLSortProperty("name", Direction.DESC))),

    CREATED_AT_ASC(List.of(new PostgreSQLSortProperty("createdAt", Direction.ASC))),
    CREATED_AT_DESC(List.of(new PostgreSQLSortProperty("createdAt", Direction.DESC))),

    PRICE_ASC(List.of(new SortJsonBProperty("price", Direction.ASC))),
    PRICE_DESC(List.of(new SortJsonBProperty("price", Direction.DESC))),

    DEFAULT_SORT(List.of(new PostgreSQLSortProperty("name", Direction.ASC)));

    private final Collection<SortProperty<Sort.Order>> properties;

    public static PostgreSQLSortOrder of(final SortProductRequest request) {
        return Objects.isNull(request) ? ProductSortOrder.DEFAULT_SORT : switch (request) {
            case NAME_ASC -> ProductSortOrder.NAME_ASC;
            case NAME_DESC -> ProductSortOrder.NAME_DESC;
            case CREATED_AT_ASC -> ProductSortOrder.CREATED_AT_ASC;
            case CREATED_AT_DESC -> ProductSortOrder.CREATED_AT_DESC;
            case PRICE_ASC -> ProductSortOrder.PRICE_ASC;
            case PRICE_DESC -> ProductSortOrder.PRICE_DESC;

            // in case if any other request's value is not handled.
            default -> ProductSortOrder.DEFAULT_SORT;
        };
    }

}
