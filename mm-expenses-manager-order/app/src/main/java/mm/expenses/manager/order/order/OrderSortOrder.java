package mm.expenses.manager.order.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.sort.SortOrder;
import mm.expenses.manager.common.beans.pagination.sort.SortProperty;
import mm.expenses.manager.order.api.order.model.SortOrderRequest;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
enum OrderSortOrder implements SortOrder {
    DEFAULT_SORT(List.of(new SortProperty("name", Direction.ASC))),
    PRICE_SUMMARY(List.of(new SortProperty("cast(o.price_summary -> 'amount' as float)", Direction.ASC, true))),
    PRODUCTS_COUNT(List.of(new SortProperty("jsonb_array_length(o.products)", Direction.ASC, true)));

    private final Collection<SortProperty> properties;

    public static SortOrder of(final SortOrderRequest request, final Boolean isDescending) {
        return Objects.isNull(request) ? OrderSortOrder.DEFAULT_SORT : switch (request) {
            case NAME -> OrderSortOrder.DEFAULT_SORT.withDirectionsDesc(isDescending);
            case PRICE_SUMMARY -> OrderSortOrder.PRICE_SUMMARY.withDirectionsDesc(isDescending);
            case PRODUCTS_COUNT -> OrderSortOrder.PRODUCTS_COUNT.withDirectionsDesc(isDescending);

            // in case if any other request's value is not handled.
            default -> OrderSortOrder.DEFAULT_SORT;
        };
    }

}
