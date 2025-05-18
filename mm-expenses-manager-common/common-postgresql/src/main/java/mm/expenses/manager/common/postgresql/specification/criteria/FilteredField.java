package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.*;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents specific field to be handled by specific {@link org.springframework.data.jpa.domain.Specification}
 * to define class fields that can be filtered by specific specification.
 * Defines the specific field name and type with additional information if the field is defined as JSONB value.
 */
@Setter
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class FilteredField {

    private String name;

    /**
     * Some fields can be recognized by different name, this field represents such alternative name by which it can be recognized even if
     * the field name is different but this alternative name is present.
     */
    private String alternativeName;

    private FieldType type;

    private Boolean isJsonBType = false;

    public FilteredField(final String name, final FieldType type, final Boolean isJsonBType) {
        this(name, null, type, isJsonBType);
    }

    public FilteredField(final String name, final String alternativeName, final FieldType type, final Boolean isJsonBType) {
        this.name = name;
        this.alternativeName = alternativeName;
        this.type = type;
        this.isJsonBType = isJsonBType;
    }

    /**
     * @return list of passed objects value mapped to specific {@link FieldType} or returns list of original
     * fieldType if the fieldType is not recognized.
     */
    public List<Object> validateValues(final String filteredFieldValue) {
        Objects.requireNonNull(filteredFieldValue, "Filtered field value cannot be null");

        val result = splitValuesSeparatedByComma(filteredFieldValue).stream()
                .filter(value -> !value.isEmpty())
                .map(value -> switch (type) {
                    case Boolean -> Boolean.valueOf(value);
                    case Long -> Long.valueOf(value);
                    case Integer -> Integer.valueOf(value);
                    case Instant -> Instant.parse(value);
                    case List -> convertToList(value);
                    default -> value;
                }).collect(Collectors.toList());

        if (FieldType.List.equals(type)) {
            return result.stream().flatMap(values -> ((Collection) values).stream()).toList();
        }
        return result;
    }

    /**
     * @return checks if filtered field is any of passed types.
     */
    public boolean isOfType(final FieldType... types) {
        Objects.requireNonNull(types, "Field types cannot be null");

        return Arrays.stream(types).anyMatch(expectedType -> Objects.equals(expectedType, type));
    }

    /**
     * @return checks if filtered field can be recognized by passed field name.
     */
    public boolean hasSameName(final String... names) {
        val availableNames = Stream.of(name, alternativeName)
                .filter(Objects::nonNull)
                .toList();
        return CollectionUtils.containsAny(availableNames, List.of(names));
    }

    /**
     * @return {@link FilteredField} based on passed field name and fieldType.
     */
    public static FilteredField of(final String fieldName, final FieldType fieldType) {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        Objects.requireNonNull(fieldType, "Field fieldType cannot be null");

        return FilteredField.builder()
                .name(fieldName)
                .type(fieldType)
                .isJsonBType(false)
                .build();
    }

    /**
     * @return {@link FilteredField} based on passed field name, alternative name and fieldType.
     */
    public static FilteredField of(final String fieldName, final String alternativeFieldName, final FieldType fieldType) {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        Objects.requireNonNull(alternativeFieldName, "Alternative field name cannot be null");
        Objects.requireNonNull(fieldType, "Field fieldType cannot be null");

        return FilteredField.builder()
                .name(fieldName)
                .alternativeName(alternativeFieldName)
                .type(fieldType)
                .isJsonBType(false)
                .build();
    }

    /**
     * Converts passed value to {@link List}.
     */
    public static List<?> convertToList(final Object value) {
        if (value.getClass().isArray()) {
            return Arrays.asList((Object[]) value);
        } else if (value instanceof Collection) {
            return new ArrayList<>((Collection<?>) value);
        }
        return new ArrayList<>(List.of(value));
    }

    /**
     * Splits passed value bye comma and returns as a {@link List}.
     */
    private List<String> splitValuesSeparatedByComma(final String ids) {
        val array = Objects.nonNull(ids) ? ids.split(",") : new String[0];
        return Arrays.stream(array).map(String::trim).collect(Collectors.toList());
    }

}
