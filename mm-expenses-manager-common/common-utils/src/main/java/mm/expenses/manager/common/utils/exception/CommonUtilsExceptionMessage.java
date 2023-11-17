package mm.expenses.manager.common.utils.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public enum CommonUtilsExceptionMessage implements ExceptionType {
    ZONE_NULL_NOT_ALLOWED("zone-cannot-be-null", "Zone of null value is not allowed."),
    INSTANT_NULL_NOT_ALLOWED("instant-cannot-be-null", "Instant of null value is not allowed."),
    LOCAL_DATE_NULL_NOT_ALLOWED("local-date-cannot-be-null", "LocalDate of null value is not allowed."),
    DATE_STRING_NULL_NOT_ALLOWED("date-string-cannot-be-null", "Date as string has null value what is not allowed."),
    DATE_STRING_CANNOT_BE_PARSED("date-string-cannot-be-parsed", "Unable to parse date as string."),
    DATE_LONG_NULL_NOT_ALLOWED("date-long-cannot-be-null", "Date as long has null value what is not allowed."),
    INSTANT_TO_LOCAL_DATE_ERROR("instant-to-local-date-error", "Instant cannot be converted to LocalDate."),
    LOCAL_DATE_TO_INSTANT_ERROR("local-date-to-instant-error", "Instant cannot be converted to LocalDate.");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return String.format(message, parameters);
    }

    @Override
    public ExceptionType withParameters(final Object... params) {
        if (Objects.nonNull(parameters) && ArrayUtils.isNotEmpty(parameters)) {
            final var tempList = new ArrayList<>(Arrays.asList(parameters));
            tempList.addAll(new ArrayList<>(Arrays.asList(params)));
            parameters = tempList.toArray();
        } else {
            parameters = params;
        }
        return this;
    }

}
