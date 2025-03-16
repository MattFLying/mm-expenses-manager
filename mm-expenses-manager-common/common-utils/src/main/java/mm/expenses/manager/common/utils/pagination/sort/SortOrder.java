package mm.expenses.manager.common.utils.pagination.sort;

import mm.expenses.manager.common.exceptions.sort.SortOrderException;
import mm.expenses.manager.common.utils.exception.CommonUtilsExceptionMessage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Interface to define specific sorting order.
 *
 * @param <S> sort type
 * @param <O> order type
 */
public interface SortOrder<S, O> {

    /**
     * Returns list of properties for specific sorting.
     */
    Collection<SortProperty<O>> getProperties();

    /**
     * Returns sorting of specific sorting type.
     */
    S getSort();

    default List<O> getOrders() {
        return Objects.isNull(getProperties())
                ? Collections.emptyList()
                : getProperties().stream().map(SortProperty::getOrder).collect(Collectors.toList());
    }

    default O getOrder() {
        final var orders = getOrders();
        if (Objects.isNull(orders) || orders.size() != 1) {
            throw new SortOrderException(CommonUtilsExceptionMessage.PAGINATION_SORT_ORDER_MULTIPLE_VALUES);
        }
        return orders.get(0);
    }

    default SortOrder<S, O> withDirectionsDesc(final Boolean isDescending) {
        if (Objects.nonNull(isDescending)) {
            for (var sortProperty : getProperties()) {
                sortProperty.setDirectionDesc(!isDescending);
            }
            return this;
        }
        return withDirectionsAsc(true);
    }

    default SortOrder<S, O> withDirectionsAsc(final Boolean isAscending) {
        if (Objects.nonNull(isAscending)) {
            for (var sortProperty : getProperties()) {
                sortProperty.setDirectionAsc(isAscending);
            }
            return this;
        }
        return withDirectionsDesc(false);
    }

}
