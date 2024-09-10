package mm.expenses.manager.common.postgresql.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import mm.expenses.manager.common.utils.sort.SortProperty;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@Getter
public class SortJsonBProperty extends SortProperty<Order> {

    private Direction springDirection;

    public SortJsonBProperty(@NotNull final String property, final Direction direction) {
        super(property, of(direction), true);
        this.springDirection = direction;
    }

    public Order getOrder() {
        return isJsonBValue
                ? JsonBSortOrder.by(getProperty(), getSpringDirection())
                : Order.by(getProperty()).with(getSpringDirection());
    }

    public static SortDirection of(final Direction direction) {
        return SortDirection.valueOf(direction.name());
    }

}
