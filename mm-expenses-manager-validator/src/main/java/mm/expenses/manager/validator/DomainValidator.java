package mm.expenses.manager.validator;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public interface DomainValidator<DOMAIN> {

    Collection<ValidatorMessage> validateDomain(final DOMAIN object);

    default String reasonOfValidationError(final Collection<ValidatorMessage> validators) {
        return validators.stream()
                .map(ValidatorMessage::getMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(StringUtils.SPACE));
    }

    default <E extends ValidationException> void checkIfObjectIsValid(final Collection<ValidatorMessage> validations, final Class<E> exceptionType) throws E {
        if (!validations.isEmpty()) {
            final var validationFailReason = reasonOfValidationError(validations);
            try {
                throw exceptionType.getConstructor(String.class).newInstance(validationFailReason);
            } catch (final InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
                throw new ValidationException(validationFailReason, exception);
            }
        }
    }

    interface CreateValidator<DOMAIN, NEW_OBJECT> extends DomainValidator<DOMAIN> {
        Collection<ValidatorMessage> validateNew(final NEW_OBJECT object);
    }

    interface UpdateValidator<DOMAIN, UPDATE_OBJECT> extends DomainValidator<DOMAIN> {
        Collection<ValidatorMessage> validateUpdate(final UPDATE_OBJECT object);
    }

    interface Validator<DOMAIN, NEW_OBJECT, UPDATE_OBJECT> extends CreateValidator<DOMAIN, NEW_OBJECT>, UpdateValidator<DOMAIN, UPDATE_OBJECT> {
    }

}
