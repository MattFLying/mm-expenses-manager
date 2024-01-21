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
    private boolean isJsonBValue = false;

    public SortProperty(@NotNull final String property, final Direction direction, final boolean isJsonBValue) {
        this.property = property;
        this.direction = direction;
        this.isJsonBValue = isJsonBValue;
    }

    public SortProperty(@NotNull final String property, final Direction direction) {
        this(property, direction, false);
    }

    public Order getOrder() {
        return isJsonBValue
                ? JsonBSortOrder.by(getProperty(), getDirection())
                : Order.by(getProperty()).with(getDirection());
    }

    public SortProperty setDirectionAsc(final Boolean isAscending) {
        if (Objects.nonNull(isAscending) && isAscending) {
            this.direction = Direction.ASC;
        } else {
            this.direction = Direction.DESC;
        }
        return this;
    }

    public SortProperty setDirectionDesc(final Boolean isDescending) {
        if (Objects.nonNull(isDescending) && isDescending) {
            this.direction = Direction.DESC;
        } else {
            this.direction = Direction.ASC;
        }
        return this;
    }

}
