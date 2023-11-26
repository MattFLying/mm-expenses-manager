package mm.expenses.manager.common.utils.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BooleanUtils {

    /**
     * Returns the value of this Boolean object as a boolean primitive or return false if object is null.
     *
     * @return boolean value of passed object or false if object is null
     */
    public static boolean booleanPrimitiveOrDefault(final Boolean value) {
        return Objects.nonNull(value) ? value : false;
    }

}
