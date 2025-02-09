package mm.expenses.manager.common.utils.sort;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public abstract class SortProperty<Order> {

    protected String property;
    protected SortDirection direction;
    protected boolean isJsonBValue = false;

    public abstract Order getOrder();

    public SortProperty<Order> setDirectionAsc(final Boolean isAscending) {
        if (Objects.nonNull(isAscending) && isAscending) {
            this.direction = SortDirection.ASC;
        } else {
            this.direction = SortDirection.DESC;
        }
        return this;
    }

    public SortProperty<Order> setDirectionDesc(final Boolean isDescending) {
        if (Objects.nonNull(isDescending) && isDescending) {
            this.direction = SortDirection.DESC;
        } else {
            this.direction = SortDirection.ASC;
        }
        return this;
    }

    public enum SortDirection {
        ASC, DESC;

        public String toLowerCase() {
            return name().toLowerCase();
        }

        public static SortDirection of(final String direction) {
            return SortDirection.valueOf(direction);
        }

    }

}
