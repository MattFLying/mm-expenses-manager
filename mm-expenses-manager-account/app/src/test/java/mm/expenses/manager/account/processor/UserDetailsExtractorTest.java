package mm.expenses.manager.account.processor;

import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class UserDetailsExtractorTest {

    private static final String FIRST_NAME_PROPERTY_NAME = "firstName";
    private static final String LAST_NAME_PROPERTY_NAME = "lastName";
    private static final String DETAILS_PROPERTY_NAME = "details";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Potato";

    private final UserDetailsExtractor userDetailsExtractor = new UserDetailsExtractor();

    @Test
    public void shouldExtractTopLevelValuesSuccessfully() {
        // given
        final var details = new HashMap<String, Object>();
        details.put(FIRST_NAME_PROPERTY_NAME, FIRST_NAME);
        details.put(LAST_NAME_PROPERTY_NAME, LAST_NAME);

        final var userResponse = new UserResponse();
        userResponse.setDetails(details);

        // when
        userDetailsExtractor.extractUserResponseDetails(userResponse);

        // then
        assertThat(userResponse.getDetails()).isNotNull()
                .containsEntry(FIRST_NAME_PROPERTY_NAME, FIRST_NAME)
                .containsEntry(LAST_NAME_PROPERTY_NAME, LAST_NAME);
    }

    @Test
    public void shouldExtractFromListOrArraySuccessfully() {
        // given
        final var details = new HashMap<String, Object>();
        details.put(FIRST_NAME_PROPERTY_NAME, List.of(FIRST_NAME, "test"));
        details.put(LAST_NAME_PROPERTY_NAME, new String[]{LAST_NAME, "Hmm"});

        final var userResponse = new UserResponse();
        userResponse.setDetails(details);

        // when
        userDetailsExtractor.extractUserResponseDetails(userResponse);

        // then
        assertThat(userResponse.getDetails()).isNotNull()
                .containsEntry(FIRST_NAME_PROPERTY_NAME, FIRST_NAME)
                .containsEntry(LAST_NAME_PROPERTY_NAME, LAST_NAME);
    }

    @Test
    public void shouldFallbackToInternalDetailsSuccessfully() {
        // given
        final var internalDetails = new HashMap<String, Object>();
        internalDetails.put("first_name", FIRST_NAME);
        internalDetails.put("last_name", LAST_NAME);

        final var details = new HashMap<String, Object>();
        details.put(DETAILS_PROPERTY_NAME, internalDetails);

        final var userResponse = new UserResponse();
        userResponse.setDetails(details);

        // when
        userDetailsExtractor.extractUserResponseDetails(userResponse);

        // then
        assertThat(userResponse.getDetails()).isNotNull()
                .containsEntry("first_name", FIRST_NAME)
                .containsEntry("last_name", LAST_NAME);
    }

    @Test
    public void shouldSetDetailsToNull_whenEmpty() {
        // given
        final var details = new HashMap<String, Object>();
        final var userResponse = new UserResponse();
        userResponse.setDetails(details);

        // when
        userDetailsExtractor.extractUserResponseDetails(userResponse);

        // then
        assertThat(userResponse.getDetails()).isNull();
    }

    @Test
    public void shouldSetDetailsToNull_whenDetailsIsNull() {
        // given
        final var userResponse = new UserResponse();
        userResponse.setDetails(null);

        // when
        userDetailsExtractor.extractUserResponseDetails(userResponse);

        // then
        assertThat(userResponse.getDetails()).isNull();
    }

    @Test
    public void shouldThrowApiConflictExceptionOnFailure() {
        // given
        final var userResponse = new UserResponse() {
            @Override
            public Map<String, Object> getDetails() {
                throw new RuntimeException("Failure");
            }
        };

        // when & then
        assertThatThrownBy(() -> userDetailsExtractor.extractUserResponseDetails(userResponse))
                .isInstanceOf(ApiConflictException.class);
    }

}
