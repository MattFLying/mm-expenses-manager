package mm.expenses.manager.common.postgresql.specification;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationParseException;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalCriteriaParameter;
import mm.expenses.manager.common.postgresql.specification.criteria.CriteriaParameter;
import mm.expenses.manager.common.postgresql.specification.criteria.FilteredField;
import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Allows to scan any class for potential {@link org.springframework.data.jpa.domain.Specification} criteria to be
 * evaluated and used in JPA repositories based on specific handler.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpecificationScanner {

    public static final String JSONB_TYPE = "jsonb";

    /**
     * Scan expected class for any potentially available filterable fields or any other available within
     * {@link org.springframework.data.jpa.domain.Specification} criteria creation.
     *
     * @return available {@link SpecificationCriteria} for expected class
     */
    public static SpecificationCriteria scan(final Class<?> classToScan) {
        val possibleFieldsToBeSelected = new LinkedList<String>();
        val possibleFieldsToBeFiltered = new LinkedList<FilteredField>();
        val possibleFieldsToBeSorted = new LinkedList<String>();

        Arrays.stream(classToScan.getDeclaredFields())
                .forEach(field -> scanField(field, possibleFieldsToBeSelected, possibleFieldsToBeFiltered, possibleFieldsToBeSorted));
        return SpecificationCriteria.of(possibleFieldsToBeFiltered, possibleFieldsToBeSelected, possibleFieldsToBeSorted);

    }

    /**
     * Scan specific {@link AdditionalCriteriaParameter} with expected class type for any potentially available filterable fields or any other available within
     * {@link org.springframework.data.jpa.domain.Specification} criteria creation.
     *
     * @return defined {@link CriteriaParameter} for expected {@link AdditionalCriteriaParameter}
     */
    static CriteriaParameter scanParameter(final AdditionalCriteriaParameter criteriaParameter, final Type[] typeArguments) {
        Objects.requireNonNull(criteriaParameter, "Passed parameter cannot be null.");
        if (ArrayUtils.isEmpty(typeArguments)) {
            throw new SpecificationParseException("Cannot process parsing empty array of type arguments.");
        }

        val type = typeArguments[0];
        val foundField = Arrays.stream(((Class<?>) type).getDeclaredFields())
                .filter(field -> {
                    boolean alternativeNamePresent = false;
                    val specificationDetails = field.getAnnotation(SpecificationDetailsAnnotation.class);
                    if (Objects.nonNull(specificationDetails)) {
                        alternativeNamePresent = StringUtils.isNotBlank(specificationDetails.name());
                    }

                    val isFieldNamePresent = StringUtils.equalsIgnoreCase(field.getName(), criteriaParameter.getName());
                    return alternativeNamePresent
                            ? isFieldNamePresent || StringUtils.equalsIgnoreCase(field.getName(), specificationDetails.name())
                            : isFieldNamePresent;
                })
                .findAny()
                .orElseThrow(() -> SpecificationParseException.additionalPropertyNotFound(criteriaParameter.getName(), ((Class<?>) type).getSimpleName()));

        var isJsonBField = false;
        val column = foundField.getAnnotation(Column.class);
        if (Objects.nonNull(column)) {
            val columnDefinition = column.columnDefinition();
            if (Objects.nonNull(columnDefinition)) {
                isJsonBField = StringUtils.equals(JSONB_TYPE, columnDefinition.toLowerCase());
            }
        }
        return new CriteriaParameter(foundField.getName(), FieldType.of(foundField), isJsonBField, getValidOperation(criteriaParameter), List.of(criteriaParameter.getValue()), false);
    }

    private static Operation getValidOperation(final AdditionalCriteriaParameter criteriaParameter) {
        return Objects.nonNull(criteriaParameter.getOperation())
                ? criteriaParameter.getOperation()
                : Objects.nonNull(criteriaParameter.getValue())
                ? Operation.equal
                : Operation.isNull;
    }

    private static void scanField(final Field field, final List<String> possibleFieldsToBeSelected, final List<FilteredField> possibleFieldsToBeFiltered, final List<String> possibleFieldsToBeSorted) {
        val column = field.getAnnotation(Column.class);
        val isColumnAnnotationPresent = Objects.nonNull(column);
        if (isColumnAnnotationPresent) {
            possibleFieldsToBeSelected.add(field.getName());
        }
        scanSpecificationDetailsForField(field, possibleFieldsToBeFiltered, possibleFieldsToBeSorted, isColumnAnnotationPresent, column);
    }

    private static void scanSpecificationDetailsForField(final Field field,
                                                         final List<FilteredField> possibleFieldsToBeFiltered,
                                                         final List<String> possibleFieldsToBeSorted,
                                                         final boolean isColumnAnnotationPresent,
                                                         final Column column) {
        val specificationDetails = field.getAnnotation(SpecificationDetailsAnnotation.class);
        if (Objects.nonNull(specificationDetails)) {
            val alternativeNamePresent = StringUtils.isNotBlank(specificationDetails.name());
            if (specificationDetails.canBeFiltered()) {
                val fieldType = FieldType.of(field);
                val filterableField = alternativeNamePresent
                        ? FilteredField.of(field.getName(), specificationDetails.name(), fieldType)
                        : FilteredField.of(field.getName(), fieldType);
                if (isColumnAnnotationPresent) {
                    val columnDefinition = column.columnDefinition();
                    if (Objects.nonNull(columnDefinition)) {
                        filterableField.setIsJsonBType(StringUtils.equals(JSONB_TYPE, columnDefinition.toLowerCase()));
                    } else if (specificationDetails.isJsonB()) {
                        filterableField.setIsJsonBType(true);
                    }
                }
                possibleFieldsToBeFiltered.add(filterableField);
            }

            if (specificationDetails.canBeSorted()) {
                possibleFieldsToBeSorted.add(field.getName());
            }
        }
    }

}
