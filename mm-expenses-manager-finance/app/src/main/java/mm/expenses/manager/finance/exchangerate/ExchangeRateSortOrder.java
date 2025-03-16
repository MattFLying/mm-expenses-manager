package mm.expenses.manager.finance.exchangerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.mongodb.pagination.sort.MongoDBSortOrder;
import mm.expenses.manager.common.mongodb.pagination.sort.MongoDBSortProperty;
import mm.expenses.manager.common.utils.pagination.sort.SortProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateSortOrder implements MongoDBSortOrder {
    DEFAULT_SORT(List.of(new MongoDBSortProperty("date", Direction.DESC)));

    private final Collection<SortProperty<Sort.Order>> properties;

}
