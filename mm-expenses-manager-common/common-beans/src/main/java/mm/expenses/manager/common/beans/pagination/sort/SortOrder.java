package mm.expenses.manager.common.beans.pagination.sort;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.*;
import java.util.stream.Collectors;

public interface SortOrder {

    Collection<SortProperty> getProperties();

    static Sort unsorted() {
        return Sort.unsorted();
    }

    default List<Order> getOrders() {
        return Objects.isNull(getProperties())
                ? Collections.emptyList()
                : getProperties().stream().map(SortProperty::getOrder).collect(Collectors.toList());
    }

    default Sort getSort() {
        return Sort.by(getOrders());
    }

    default SortOrder withDirectionsDesc(final Boolean isDescending) {
        if (Objects.nonNull(isDescending)) {
            for (var sortProperty : getProperties()) {
                if (!sortProperty.getDirection().isDescending() && isDescending) {
                    sortProperty = sortProperty.setDirectionDesc(true);
                }
            }
        }
        return this;
    }

    default SortOrder withDirectionsAsc(final Boolean isAscending) {
        if (Objects.nonNull(isAscending)) {
            for (var sortProperty : getProperties()) {
                if (!sortProperty.getDirection().isAscending() && isAscending) {
                    sortProperty = sortProperty.setDirectionAsc(true);
                }
            }
        }
        return this;
    }

}
