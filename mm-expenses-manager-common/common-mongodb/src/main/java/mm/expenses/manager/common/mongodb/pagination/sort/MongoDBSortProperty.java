package mm.expenses.manager.common.mongodb.pagination.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import mm.expenses.manager.common.utils.pagination.sort.DefaultSortProperty;
import org.springframework.data.domain.Sort;

@Getter
public class MongoDBSortProperty extends DefaultSortProperty<Sort.Direction, Sort.Order> {

    public MongoDBSortProperty(@NotNull final String property, final Sort.Direction direction) {
        super(property, direction);
    }

    public Sort.Order getOrder() {
        return Sort.Order.by(getProperty()).with(getDirection() == null ? getSpecificDirection() : Sort.Direction.fromString(getDirection().name()));
    }

}
