package mm.expenses.manager.finance.exchangerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.sort.SortProperty;
import mm.expenses.manager.common.web.pagination.sort.SortOrder;
import mm.expenses.manager.common.web.pagination.sort.DefaultSortProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateSortOrder implements SortOrder {
    DEFAULT_SORT(List.of(new DefaultSortProperty("date", Direction.DESC)));

    private final Collection<SortProperty<Sort.Order>> properties;

}
