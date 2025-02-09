package mm.expenses.manager.order.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.postgresql.sort.SortJsonBProperty;
import mm.expenses.manager.common.utils.sort.SortProperty;
import mm.expenses.manager.common.web.pagination.sort.SortOrder;
import mm.expenses.manager.common.web.pagination.sort.DefaultSortProperty;
import mm.expenses.manager.order.api.order.model.SortOrderRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Defines sorting for {@link Order}.
 */
@Getter
@RequiredArgsConstructor
enum OrderSortOrder implements SortOrder {
    NAME_ASC(List.of(new DefaultSortProperty("name", Direction.ASC))),
    NAME_DESC(List.of(new DefaultSortProperty("name", Direction.DESC))),

    CREATED_AT_ASC(List.of(new DefaultSortProperty("createdAt", Direction.ASC))),
    CREATED_AT_DESC(List.of(new DefaultSortProperty("createdAt", Direction.DESC))),

    PRODUCTS_COUNT_ASC(List.of(new SortJsonBProperty("products", Direction.ASC))),
    PRODUCTS_COUNT_DESC(List.of(new SortJsonBProperty("products", Direction.DESC))),

    DEFAULT_SORT(List.of(new DefaultSortProperty("name", Direction.ASC)));

    private final Collection<SortProperty<Sort.Order>> properties;

    /**
     * @return proper sorting for {@link Order} based on passed requested sorting.
     */
    public static SortOrder of(final SortOrderRequest request) {
        return Objects.isNull(request) ? OrderSortOrder.DEFAULT_SORT : switch (request) {
            case NAME_ASC -> OrderSortOrder.NAME_ASC;
            case NAME_DESC -> OrderSortOrder.NAME_DESC;
            case CREATED_AT_ASC -> OrderSortOrder.CREATED_AT_ASC;
            case CREATED_AT_DESC -> OrderSortOrder.CREATED_AT_DESC;
            case PRODUCTS_COUNT_ASC -> OrderSortOrder.PRODUCTS_COUNT_ASC;
            case PRODUCTS_COUNT_DESC -> OrderSortOrder.PRODUCTS_COUNT_DESC;

            // in case if any other request's value is not handled.
            default -> OrderSortOrder.DEFAULT_SORT;
        };
    }

}
