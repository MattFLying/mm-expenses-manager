package mm.expenses.manager.common.utils.pagination.sort;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public abstract class DefaultSortProperty<D, O> extends SortProperty<O> {

    protected D specificDirection;

    public DefaultSortProperty(@NotNull final String property, final D direction, final boolean isJsonBValue) {
        super(property, of(direction), isJsonBValue);
        this.specificDirection = direction;
    }

    public DefaultSortProperty(@NotNull final String property, final D direction) {
        this(property, direction, false);
    }

}
