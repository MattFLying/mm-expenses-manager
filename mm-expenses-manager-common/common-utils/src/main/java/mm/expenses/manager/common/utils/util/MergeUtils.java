package mm.expenses.manager.common.utils.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergeUtils {

    public static <T> T firstWins(final T oldValue, final T newValue) {
        return oldValue;
    }

}
