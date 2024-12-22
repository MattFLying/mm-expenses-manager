package mm.expenses.manager.common.postgresql.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.val;
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
        val direction = getDirection() == null ? getSpringDirection() : Direction.fromString(getDirection().name());
        return isJsonBValue
                ? JsonBSortOrder.by(getProperty(), direction)
                : Order.by(getProperty()).with(direction);
    }

    public static SortDirection of(final Direction direction) {
        return SortDirection.valueOf(direction.name());
    }

}
