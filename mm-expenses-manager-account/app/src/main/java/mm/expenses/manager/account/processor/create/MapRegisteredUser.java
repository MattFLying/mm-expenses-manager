package mm.expenses.manager.account.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.CreateUserModel;
import mm.expenses.manager.account.core.user.RegisterUserBySPI;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.Instant;
import java.util.*;

import static mm.expenses.manager.account.processor.create.FindRegisteredUser.USER_KEYCLOAK_ID;

/**
 * Step in chain of new {@link User} creation.
 * Maps requested user from the event message directly to the new user stored in internal database.
 */
@Slf4j
@RequiredArgsConstructor
final class MapRegisteredUser extends CreateUserChain {

    final static String USER_DETAILS = "details";
    final static String USER_ATTRIBUTES = "attributes";
    final static String USER_RAW_DATA = "rawDataCreation";
    final static String PREPARED_USER_TO_SAVE = "preparedUserToSave";

    final static String FIRST_NAME_DASHED = "first_name";
    final static String FIRST_NAME = "firstName";
    final static String LAST_NAME_DASHED = "last_name";
    final static String LAST_NAME = "lastName";

    private final UserRepository repository;
    private final Instant creationTime;

    @Override
    public User handleRequest(final CreateUserModel message) {
        try {
            final var user = new User();
            prepareInitialUserData(message, user);
            prepareUserDetails(message, user);

            context.addArguments(Map.of(PREPARED_USER_TO_SAVE, user));

            if (!hasNext()) {
                setNextHandler(new InitializeUserPermissionsUser(repository));
            }
            return next.handleRequest(message);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown user creation error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        }
    }

    private void prepareInitialUserData(final CreateUserModel message, final User user) {
        user.setKeycloakId((UUID) context.getArgument(USER_KEYCLOAK_ID).orElseGet(() -> UUID.fromString(message.getUserId())));

        configureEmail(user, message.getEmail());
        user.setUsername(message.getUsername().trim());

        user.setEmailVerified(message.getIsEmailVerified());
        user.setEnabled(message.getIsEnabled());

        configureCreationTime(user, message);
    }

    private void configureEmail(final User user, final String email) {
        final var isEmailValid = EmailValidator.getInstance().isValid(email);
        if (isEmailValid) {
            user.setEmail(email.trim());
        } else {
            log.error("New user creation failed because of invalid email address={}.", email);
            throw new ApiValidationException(UserExceptionMessage.USER_EMAIL_INVALID);
        }
    }

    private void configureCreationTime(final User user, final CreateUserModel message) {
        // creation time directly in the keycloak
        final var originalCreationTime = Instant.ofEpochMilli(message.getTime());

        // in case if there is no creation time from keycloak just use the time when creation process started
        final var expectedCreationTime = Objects.nonNull(originalCreationTime) ? originalCreationTime : creationTime;
        user.setCreatedAt(expectedCreationTime);
        user.setLastModifiedAt(expectedCreationTime);
    }

    private void prepareUserDetails(final CreateUserModel message, final User user) {
        final var userDetails = new HashMap<String, Object>();
        final var details = Objects.nonNull(message.getDetails()) ? message.getDetails() : new HashMap<String, String>();
        final var attributes = Objects.nonNull(message.getAttributes()) ? message.getAttributes() : new HashMap<String, List<String>>();

        if (message instanceof RegisterUserBySPI spiApiRequest) {
            spiApiRequest.getAttributes()
                    .getFirstName()
                    .ifPresent(firstName -> userDetails.put(FIRST_NAME, firstName));

            spiApiRequest.getAttributes()
                    .getLastName()
                    .ifPresent(lastName -> userDetails.put(LAST_NAME, lastName));
        } else {
            extractValue(FIRST_NAME_DASHED, FIRST_NAME, details, attributes).ifPresent(firstName -> userDetails.put(FIRST_NAME, firstName));
            extractValue(LAST_NAME_DASHED, LAST_NAME, details, attributes).ifPresent(lastName -> userDetails.put(LAST_NAME, lastName));
        }

        final var rawData = new HashMap<String, Object>();
        rawData.put(USER_DETAILS, message.getDetails());
        rawData.put(USER_ATTRIBUTES, message.getAttributes());

        userDetails.put(USER_RAW_DATA, rawData);
        user.setDetails(userDetails);

        context.addArguments(Map.of(USER_DETAILS, user.getDetails()));
    }

    private Optional<String> extractValue(final String expectedKeyInRawDetails, final String expectedKeyInRawAttributes, final Map<String, String> details, final Map<String, List<String>> attributes) {
        // when details in raw data is present check if there is an expected key present and return its value otherwise
        if (Objects.nonNull(details)) {
            final var firstName = details.get(expectedKeyInRawDetails);
            if (Objects.nonNull(firstName)) {
                return Optional.of(firstName);
            }
        }

        // check if there is related property present in the attributes otherwise returns empty value
        final var firstNameFromAttributes = attributes.get(expectedKeyInRawAttributes);
        if (Objects.nonNull(firstNameFromAttributes) && !firstNameFromAttributes.isEmpty()) {
            return Optional.of(firstNameFromAttributes.get(0));
        }

        return Optional.empty();
    }

}
