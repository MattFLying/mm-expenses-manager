package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents specification criteria of specific class with possible fields to be filtered, sorted and selected in
 * {@link org.springframework.data.jpa.domain.Specification} queries together with possible pagination.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SpecificationCriteria {

    private PaginationConfig.Pagination pagination;

    private List<FilteredField> filteredFields;

    private List<String> selectedFields;

    private List<String> sortedFields;

    /**
     * Configures pagination for specification criteria to be used during {@link org.springframework.data.jpa.domain.Specification} handling.
     */
    public void setPagination(final PaginationConfig config) {
        if (Objects.isNull(config)) {
            throw new SpecificationCriteriaException(SpecificationCriteriaException.PAGINATION_CONFIG_IS_NULL_MESSAGE);
        }
        this.pagination = PaginationConfig.Pagination.of(config.getMinPageNumber(), config.getDefaultPageSize(), config.getMaxPageSize());
    }

    /**
     * @return {@link FilteredField} of specific field name.
     */
    public FilteredField getFilteredField(final String nameOfFilteredField) {
        Objects.requireNonNull(nameOfFilteredField, "Name of filtered field cannot be null");
        if (CollectionUtils.isNotEmpty(filteredFields)) {
            for (val field : filteredFields) {
                if (StringUtils.equals(nameOfFilteredField, field.getName())) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * @return true if passed field name can be filtered, otherwise returns false.
     */
    public boolean canFieldBeSorted(final String name) {
        Objects.requireNonNull(name, "Sorted field name cannot be null");
        return Objects.nonNull(sortedFields) && sortedFields.contains(name);
    }

    /**
     * @return {@link SpecificationCriteria} of passed possible filtered fields, selectable and sortable fields.
     */
    public static SpecificationCriteria of(final List<FilteredField> filteredField, final List<String> selectedFields, final List<String> sortedFields) {
        return SpecificationCriteria.builder()
                .filteredFields(filteredField)
                .selectedFields(selectedFields)
                .sortedFields(sortedFields)
                .build();
    }

}
