package mm.expenses.manager.common.web.pagination.sort;

import mm.expenses.manager.common.exceptions.sort.SortOrderException;
import mm.expenses.manager.common.utils.sort.SortProperty;
import mm.expenses.manager.common.web.exception.WebExceptionMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.*;
import java.util.stream.Collectors;

public interface SortOrder {

    Collection<SortProperty<Order>> getProperties();

    static Sort unsorted() {
        return Sort.unsorted();
    }

    default List<Order> getOrders() {
        return Objects.isNull(getProperties())
                ? Collections.emptyList()
                : getProperties().stream().map(SortProperty::getOrder).collect(Collectors.toList());
    }

    default Order getOrder() {
        final var orders = getOrders();
        if (Objects.isNull(orders) || orders.size() != 1) {
            throw new SortOrderException(WebExceptionMessage.PAGINATION_SORT_ORDER_MULTIPLE_VALUES);
        }
        return orders.get(0);
    }

    default Sort getSort() {
        return Sort.by(getOrders());
    }

    default SortOrder withDirectionsDesc(final Boolean isDescending) {
        if (Objects.nonNull(isDescending)) {
            for (var sortProperty : getProperties()) {
                sortProperty.setDirectionDesc(!isDescending);
            }
            return this;
        }
        return withDirectionsAsc(true);
    }

    default SortOrder withDirectionsAsc(final Boolean isAscending) {
        if (Objects.nonNull(isAscending)) {
            for (var sortProperty : getProperties()) {
                sortProperty.setDirectionAsc(isAscending);
            }
            return this;
        }
        return withDirectionsDesc(false);
    }

}
