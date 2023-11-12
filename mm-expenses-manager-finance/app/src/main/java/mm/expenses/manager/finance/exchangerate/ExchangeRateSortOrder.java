package mm.expenses.manager.finance.exchangerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.sort.SortOrder;
import mm.expenses.manager.common.beans.pagination.sort.SortProperty;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateSortOrder implements SortOrder {
    DEFAULT_SORT(List.of(new SortProperty("date", Direction.DESC)));

    private final Collection<SortProperty> properties;

}
