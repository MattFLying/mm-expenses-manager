package mm.expenses.manager.product.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.sort.SortOrder;
import mm.expenses.manager.common.beans.pagination.sort.SortProperty;
import mm.expenses.manager.product.api.product.model.SortOrderRequest;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
enum ProductSortOrder implements SortOrder {
    DEFAULT_SORT(List.of(new SortProperty("name", Direction.ASC))),
    PRICE_VALUE(List.of(new SortProperty("cast(p.price -> 'value' as float)", Direction.ASC, true)));

    private final Collection<SortProperty> properties;

    public static SortOrder of(final SortOrderRequest request, final Boolean isDescending) {
        return Objects.isNull(request) ? ProductSortOrder.DEFAULT_SORT : switch (request) {
            case NAME -> ProductSortOrder.DEFAULT_SORT.withDirectionsDesc(isDescending);
            case PRICE_VALUE -> ProductSortOrder.PRICE_VALUE.withDirectionsDesc(isDescending);

            // in case if any other request's value is not handled.
            default -> ProductSortOrder.DEFAULT_SORT;
        };
    }

}
