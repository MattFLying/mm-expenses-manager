package mm.expenses.manager.common.postgresql.specification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.utils.config.ObjectMapperConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Representation of field type defined in entities or another classes to be used during defining
 * {@link org.springframework.data.jpa.domain.Specification} definition to correctly handle
 * specific field's types in expected queries.
 */
@Getter
@RequiredArgsConstructor
public enum FieldType {
    String(String.class, "String"),
    Boolean(Boolean.class, "boolean"),
    Long(Long.class, "long"),
    Integer(Integer.class, "int"),
    Instant(java.time.Instant.class, "Instant"),
    List(java.util.List.class, "List");

    private static final ObjectMapper objectMapper = ObjectMapperConfig.objectMapper();

    private final Class<?> type;
    private final String simpleName;

    public String getName() {
        return name();
    }

    /**
     * @return {@link FieldType} of given {@link Field} based on the original field type.
     */
    public static FieldType of(final Field field) {
        val fieldType = Objects.requireNonNull(field, "Field cannot be null").getType().getSimpleName();
        return valueOf(
                switch (fieldType) {
                    case "int":
                        yield Integer.getName();
                    case "long":
                        yield Long.getName();
                    case "boolean":
                        yield Boolean.getName();
                    default:
                        if (Collection.class.isAssignableFrom(field.getType())) {
                            yield List.getName();
                        }
                        yield fieldType;
                }
        );
    }

    /**
     * @return the expected field's value as expected {@link FieldType} fieldType.
     */
    public static Object valueOf(final String value, final FieldType expectedType) {
        Objects.requireNonNull(value, "Value cannot be null");
        Objects.requireNonNull(expectedType, "Expected fieldType cannot be null");

        return switch (expectedType) {
            case Integer:
                yield java.lang.Integer.valueOf(value);
            case Long:
                yield java.lang.Long.valueOf(value);
            case Boolean:
                yield java.lang.Boolean.valueOf(value);
            case Instant:
                yield java.time.Instant.parse(value);
            case List:
                try {
                    yield objectMapper.readValue(value, java.util.List.class);
                } catch (final JsonProcessingException exception) {
                    yield new ArrayList<>(java.util.List.of(value));
                }
            default:
                yield value;
        };
    }

}
