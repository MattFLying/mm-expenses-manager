package mm.expenses.manager.common.beans.pagination.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public final class SortProperty {

    private final String property;
    private Direction direction;

    public SortProperty(@NotNull final String property, final Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public Order getOrder() {
        return Order.by(getProperty()).with(getDirection());
    }

    public SortProperty setDirectionAsc(final Boolean isAscending) {
        if (Objects.nonNull(isAscending) && isAscending) {
            this.direction = Direction.ASC;
        }
        return this;
    }

    public SortProperty setDirectionDesc(final Boolean isDescending) {
        if (Objects.nonNull(isDescending) && isDescending) {
            this.direction = Direction.DESC;
        }
        return this;
    }

}
