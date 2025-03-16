package mm.expenses.manager.common.mongodb.pagination.sort;

import mm.expenses.manager.common.utils.pagination.sort.SortOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public interface MongoDBSortOrder extends SortOrder<Sort, Order> {

    static Sort unsorted() {
        return Sort.unsorted();
    }

    default Sort getSort() {
        return Sort.by(getOrders());
    }

}
