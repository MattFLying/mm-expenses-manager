package mm.expenses.manager.common.postgresql.exception;

import mm.expenses.manager.common.postgresql.specification.criteria.CriteriaParameter;

/**
 * Specific exception to be thrown during any parse processing for {@link org.springframework.data.jpa.domain.Specification}
 */
public class SpecificationParseException extends RuntimeException {

    public static final String INVALID_QUERIED_PARAMETER = "Incorrect value format for queried parameter: %s with value: %s";
    public static final String CANNOT_SORT_BY_FIELD = "Sorting is not available for queried parameter: %s";
    public static final String CONVERTER_NOT_FOUND = "Converter for criteria: [%s] not found";
    public static final String INCORRECT_ADDITIONAL_FIELD_NAME = "Property: [%s] not found for type: [%s]";

    public SpecificationParseException(final String message) {
        super(message);
    }

    public SpecificationParseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public static SpecificationParseException incorrectValueFormatForField(final String parameterName, final Object value, final Throwable throwable) {
        return new SpecificationParseException(String.format(INVALID_QUERIED_PARAMETER, parameterName, value), throwable);
    }

    public static SpecificationParseException fieldCannotBeSorted(final String parameterName) {
        return new SpecificationParseException(String.format(CANNOT_SORT_BY_FIELD, parameterName));
    }

    public static SpecificationParseException converterNotFound(final CriteriaParameter criteriaParameter) {
        return new SpecificationParseException(String.format(CONVERTER_NOT_FOUND, criteriaParameter));
    }

    public static SpecificationParseException additionalPropertyNotFound(final String propertyName, final String typeName) {
        return new SpecificationParseException(String.format(INCORRECT_ADDITIONAL_FIELD_NAME, propertyName, typeName));
    }

}
