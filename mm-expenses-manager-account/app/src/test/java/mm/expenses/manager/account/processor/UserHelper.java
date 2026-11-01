package mm.expenses.manager.account.processor;

import lombok.val;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.utils.util.DateUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.time.Instant;
import java.util.*;

public class UserHelper {

    private static final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    public static final UUID ID = UUID.randomUUID();
    public static final UUID KEYCLOAK_ID = UUID.randomUUID();
    public static final String USER_NAME = UUID.randomUUID().toString();
    public static final String EMAIL = "email@test.com";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Tricky";
    public static final Map<String, Object> PRODUCT_DETAILS = new HashMap<>();

    public static User createUser(final boolean isDeleted) {
        final var now = DateUtils.nowAsInstant();

        return createUser(now, isDeleted);
    }

    public static User createUser() {
        return createUser(false);
    }

    private static User createUser(final Instant now, final boolean isDeleted) {
        PRODUCT_DETAILS.put("firstName", FIRST_NAME);
        PRODUCT_DETAILS.put("lastName", LAST_NAME);

        return User.builder()
                .id(ID)
                .keycloakId(KEYCLOAK_ID)
                .username(USER_NAME)
                .email(EMAIL)
                .details(PRODUCT_DETAILS)
                .isDeleted(isDeleted)
                .isEmailVerified(true)
                .isEnabled(true)
                .createdAt(now)
                .lastModifiedAt(now)
                .version(1L)
                .build();
    }

}
