package mm.expenses.manager.account.processor;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link User}'s details extractor.
 * Simplifies the details if needed in specific manner. Can hide some data or even extend it.
 */
@Slf4j
public class UserDetailsExtractor {

    private static final String DETAILS_KEY = "details";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";

    public void extractUserResponseDetails(final UserResponse userResponse) {
        final var newDetails = new HashMap<String, Object>();

        try {
            final var fullDetails = userResponse.getDetails();
            if (MapUtils.isNotEmpty(fullDetails)) {
                final var internalDetails = (Map<String, Object>) fullDetails.getOrDefault(DETAILS_KEY, new HashMap<String, Object>());

                extractTextValue(fullDetails, internalDetails, newDetails, FIRST_NAME_KEY);
                extractTextValue(fullDetails, internalDetails, newDetails, LAST_NAME_KEY);
            }
        } catch (final Exception exception) {
            log.error("Unknown user error occurred during data extraction.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_DETAILS_EXTRACTION, exception);
        } finally {
            if (!newDetails.isEmpty()) {
                // replace current details with the newly prepared shorter version
                userResponse.setDetails(newDetails);
            } else {
                // hide the details to do not leak anything
                userResponse.setDetails(null);
            }
        }
    }

    private void extractTextValue(final Map<String, Object> fullDetails, final Map<String, Object> internalDetails, final Map<String, Object> newDetails, final String keyTopLevel) {
        final var keyInternalDetailsLevelName = keyTopLevel.replaceAll("([A-Z])", "_$1").toLowerCase();
        final var valueAtTopLevel = fullDetails.getOrDefault(keyTopLevel, null);

        if (Objects.nonNull(valueAtTopLevel)) {
            if (valueAtTopLevel instanceof List<?> list) {
                newDetails.put(keyTopLevel, list.get(0));
            } else if (valueAtTopLevel instanceof String[] array) {
                newDetails.put(keyTopLevel, array[0]);
            } else {
                newDetails.put(keyTopLevel, valueAtTopLevel);
            }
        } else {
            final var internalValue = internalDetails.getOrDefault(keyInternalDetailsLevelName, null);
            if (Objects.nonNull(internalValue)) {
                newDetails.put(keyInternalDetailsLevelName, internalValue);
            }
        }
    }

}
