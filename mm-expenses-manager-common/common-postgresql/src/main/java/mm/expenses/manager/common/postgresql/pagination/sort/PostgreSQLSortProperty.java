package mm.expenses.manager.common.postgresql.pagination.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import mm.expenses.manager.common.utils.pagination.sort.DefaultSortProperty;
import org.springframework.data.domain.Sort;

@Getter
public class PostgreSQLSortProperty extends DefaultSortProperty<Sort.Direction, Sort.Order> {

    public PostgreSQLSortProperty(@NotNull final String property, final Sort.Direction direction) {
        super(property, direction);
    }

    public PostgreSQLSortProperty(@NotNull final String property, final Sort.Direction direction, final boolean isJsonBValue) {
        super(property, direction, isJsonBValue);
    }

    public Sort.Order getOrder() {
        return Sort.Order.by(getProperty()).with(getDirection() == null ? getSpecificDirection() : Sort.Direction.fromString(getDirection().name()));
    }

}
