package mm.expenses.manager.common.utils.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdUtils {

    public static UUID generateIdAsUUID() {
        return UUID.randomUUID();
    }

    public static String generateId() {
        return generateIdAsUUID().toString();
    }

}
