package mm.expenses.manager.product.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.postgresql.sort.SortJsonBProperty;
import mm.expenses.manager.common.utils.sort.SortProperty;
import mm.expenses.manager.common.web.pagination.sort.SortOrder;
import mm.expenses.manager.common.web.pagination.sort.DefaultSortProperty;
import mm.expenses.manager.product.api.product.model.SortOrderRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
enum ProductSortOrder implements SortOrder {
    DEFAULT_SORT(List.of(new DefaultSortProperty("name", Direction.ASC))),
    PRICE_VALUE(List.of(new SortJsonBProperty("cast(p.price -> 'value' as float)", Direction.ASC)));

    private final Collection<SortProperty<Sort.Order>> properties;

    public static SortOrder of(final SortOrderRequest request, final Boolean isDescending) {
        return Objects.isNull(request) ? ProductSortOrder.DEFAULT_SORT : switch (request) {
            case NAME -> ProductSortOrder.DEFAULT_SORT.withDirectionsDesc(isDescending);
            case PRICE_VALUE -> ProductSortOrder.PRICE_VALUE.withDirectionsDesc(isDescending);

            // in case if any other request's value is not handled.
            default -> ProductSortOrder.DEFAULT_SORT;
        };
    }

}
