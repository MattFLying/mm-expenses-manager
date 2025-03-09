package mm.expenses.manager.common.postgresql.specification;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.postgresql.specification.criteria.FilteredField;
import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
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
            if (specificationDetails.canBeFiltered()) {
                val fieldType = FieldType.of(field);
                val filterableField = FilteredField.of(field.getName(), fieldType);
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
