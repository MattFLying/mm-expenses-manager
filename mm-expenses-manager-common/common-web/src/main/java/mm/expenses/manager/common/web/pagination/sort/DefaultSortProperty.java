package mm.expenses.manager.common.web.pagination.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import mm.expenses.manager.common.utils.sort.SortProperty;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@Getter
public class DefaultSortProperty extends SortProperty<Order> {

    private Direction springDirection;

    public DefaultSortProperty(@NotNull final String property, final Direction direction) {
        super(property, of(direction), false);
        this.springDirection = direction;
    }

    public Order getOrder() {
        return Order.by(getProperty()).with(getDirection() == null ? getSpringDirection() : Direction.fromString(getDirection().name()));
    }

    public static SortDirection of(final Direction direction) {
        return SortDirection.valueOf(direction.name());
    }

}
