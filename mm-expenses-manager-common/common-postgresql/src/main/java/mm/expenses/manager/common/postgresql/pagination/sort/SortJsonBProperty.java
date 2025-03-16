package mm.expenses.manager.common.postgresql.pagination.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.val;
import mm.expenses.manager.common.utils.pagination.sort.SortProperty;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@Getter
public class SortJsonBProperty extends SortProperty<Order> {

    private Direction jsonBDirection;

    public SortJsonBProperty(@NotNull final String property, final Direction direction) {
        super(property, of(direction), true);
        this.jsonBDirection = direction;
    }

    public Order getOrder() {
        val direction = getDirection() == null ? getJsonBDirection() : Direction.fromString(getDirection().name());
        return isJsonBValue
                ? JsonBSortOrder.by(getProperty(), direction)
                : Order.by(getProperty()).with(direction);
    }

}
