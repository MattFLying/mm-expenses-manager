package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.FieldTypeArgument;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecificationCriteriaTest {

    @Test
    void setPagination_shouldCreateDefaultPagination() {
        // given
        val paginationConfig = new PaginationConfig();

        val specificationCriteria = new SpecificationCriteria(null, null, null, null);

        // when
        specificationCriteria.setPagination(paginationConfig);

        // then
        assertThat(specificationCriteria).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        val specificationPaginationConfig = specificationCriteria.getPagination();
        assertThat(specificationPaginationConfig).isNotNull();
        assertThat(specificationPaginationConfig.getPageSize()).isNotNull()
                .isEqualTo(PaginationConfig.DEFAULT_PAGE_SIZE);
        assertThat(specificationPaginationConfig.getPageNumber()).isNotNull()
                .isEqualTo(PaginationConfig.DEFAULT_PAGE_NUMBER);
        assertThat(specificationPaginationConfig.getMaxPageSize()).isNotNull()
                .isEqualTo(PaginationConfig.DEFAULT_MAX_PAGE_SIZE);
    }

    @Test
    void setPagination_shouldCreatePagination() {
        // given
        val defaultPageSize = 12;
        val defaultMinPageNumber = 13;
        val defaultMaxPageSize = 14;

        val paginationConfig = new PaginationConfig();
        paginationConfig.setDefaultPageSize(defaultPageSize);
        paginationConfig.setMinPageNumber(defaultMinPageNumber);
        paginationConfig.setMaxPageSize(defaultMaxPageSize);

        val specificationCriteria = new SpecificationCriteria(null, null, null, null);

        // when
        specificationCriteria.setPagination(paginationConfig);

        // then
        assertThat(specificationCriteria).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        val specificationPaginationConfig = specificationCriteria.getPagination();
        assertThat(specificationPaginationConfig).isNotNull();
        assertThat(specificationPaginationConfig.getPageSize()).isNotNull()
                .isEqualTo(defaultPageSize);
        assertThat(specificationPaginationConfig.getPageNumber()).isNotNull()
                .isEqualTo(defaultMinPageNumber);
        assertThat(specificationPaginationConfig.getMaxPageSize()).isNotNull()
                .isEqualTo(defaultMaxPageSize);
    }

    @Test
    void setPagination_shouldThrowSpecificationCriteriaException_whenPaginationConfigIsNull() {
        // given
        val specificationCriteria = new SpecificationCriteria(null, null, null, null);

        // when & then
        assertThatThrownBy(() -> specificationCriteria.setPagination(null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(SpecificationCriteriaException.PAGINATION_CONFIG_IS_NULL_MESSAGE);
    }

    @Test
    void getFilteredField_shouldThrowNPE_whenFilteredFieldValueIsNull() {
        // given
        val specificationCriteria = new SpecificationCriteria(null, null, null, null);

        // when & then
        assertThatThrownBy(() -> specificationCriteria.getFilteredField(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name of filtered field cannot be null");
    }

    @Test
    void getFilteredField_shouldReturnNull_whenFieldOfGivenNameDoesNotExists() {
        // given
        val nameOfFilteredField = "name";
        val specificationCriteria = new SpecificationCriteria(null, null, null, null);

        // when
        val result = specificationCriteria.getFilteredField(nameOfFilteredField);

        // then
        assertThat(specificationCriteria).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(specificationCriteria.getFilteredFields()).isNull();
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void getFilteredField_shouldReturnFieldByName(final FieldType type) {
        // given
        val nameOfFilteredField = "name";
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val listOfFilteredFields = List.of(filteredField);
        val specificationCriteria = new SpecificationCriteria(null, listOfFilteredFields, null, null);

        // when
        val result = specificationCriteria.getFilteredField(nameOfFilteredField);

        // then
        assertThat(specificationCriteria).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(specificationCriteria.getFilteredFields()).isNotNull();

        assertThat(result).isNotNull().isInstanceOf(FilteredField.class);
        assertThat(result.getName()).isEqualTo(nameOfFilteredField);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getIsJsonBType()).isFalse();
    }

    @Test
    void canFieldBeSorted_shouldThrowNPE_whenPassedFieldIsNull() {
        // given
        val specificationCriteria = new SpecificationCriteria(null, null, null, null);

        // when & then
        assertThatThrownBy(() -> specificationCriteria.canFieldBeSorted(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Sorted field name cannot be null");
    }

    @Test
    void canFieldBeSorted_shouldReturnTrue_whenFieldCanBeSorted() {
        // given
        val nameOfSortedField = "name";

        val listOfSortedFields = List.of(nameOfSortedField);
        val specificationCriteria = new SpecificationCriteria(null, null, null, listOfSortedFields);

        // when
        val result = specificationCriteria.canFieldBeSorted(nameOfSortedField);

        // then
        assertThat(specificationCriteria).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(specificationCriteria.getSortedFields()).isNotNull()
                .isNotEmpty();

        assertThat(result).isNotNull()
                .isInstanceOf(Boolean.class)
                .isTrue();
    }

    @Test
    void canFieldBeSorted_shouldReturnFalse_whenFieldCanNotBeSorted() {
        // given
        val nameOfNotSortedField = "test";
        val nameOfSortedField = "name";

        val listOfSortedFields = List.of(nameOfSortedField);
        val specificationCriteria = new SpecificationCriteria(null, null, null, listOfSortedFields);

        // when
        val result = specificationCriteria.canFieldBeSorted(nameOfNotSortedField);

        // then
        assertThat(specificationCriteria).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(specificationCriteria.getSortedFields()).isNotNull()
                .isNotEmpty();

        assertThat(result).isNotNull()
                .isInstanceOf(Boolean.class)
                .isFalse();
    }

    @Test
    void of_shouldReturnSpecificationCriteria_withAllNullValues() {
        // given & when
        val result = SpecificationCriteria.of(null, null, null);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(result.getFilteredFields()).isNull();
        assertThat(result.getSelectedFields()).isNull();
        assertThat(result.getSortedFields()).isNull();
        assertThat(result.getPagination()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void of_shouldReturnSpecificationCriteria_withAllDefinedFieldsWithoutPagination(final FieldType type) {
        // given & when
        val nameOfFilteredField = "name";
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val nameSelectedAndSorted = "test";

        val filteredFields = List.of(filteredField);
        val selectedFields = List.of(nameSelectedAndSorted);
        val sortedFields = List.of(nameSelectedAndSorted);
        val result = SpecificationCriteria.of(filteredFields, selectedFields, sortedFields);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(result.getFilteredFields()).isNotNull()
                .hasSize(1)
                .containsExactly(filteredField);
        assertThat(result.getSelectedFields()).isNotNull()
                .hasSize(1)
                .containsExactly(nameSelectedAndSorted);
        assertThat(result.getSortedFields()).isNotNull()
                .hasSize(1)
                .containsExactly(nameSelectedAndSorted);
        assertThat(result.getPagination()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void of_shouldReturnSpecificationCriteria_withAllDefinedFieldsWithPagination(final FieldType type) {
        // given & when
        val nameOfFilteredField = "name";
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val nameSelectedAndSorted = "test";

        val defaultPageSize = 12;
        val defaultMinPageNumber = 13;
        val defaultMaxPageSize = 14;

        val paginationConfig = new PaginationConfig();
        paginationConfig.setDefaultPageSize(defaultPageSize);
        paginationConfig.setMinPageNumber(defaultMinPageNumber);
        paginationConfig.setMaxPageSize(defaultMaxPageSize);

        val filteredFields = List.of(filteredField);
        val selectedFields = List.of(nameSelectedAndSorted);
        val sortedFields = List.of(nameSelectedAndSorted);
        val result = SpecificationCriteria.of(filteredFields, selectedFields, sortedFields);
        result.setPagination(paginationConfig);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(SpecificationCriteria.class);

        assertThat(result.getFilteredFields()).isNotNull()
                .hasSize(1)
                .containsExactly(filteredField);
        assertThat(result.getSelectedFields()).isNotNull()
                .hasSize(1)
                .containsExactly(nameSelectedAndSorted);
        assertThat(result.getSortedFields()).isNotNull()
                .hasSize(1)
                .containsExactly(nameSelectedAndSorted);
        assertThat(result.getPagination()).isNotNull();

        val specificationPaginationConfig = result.getPagination();
        assertThat(specificationPaginationConfig).isNotNull();
        assertThat(specificationPaginationConfig.getPageSize()).isNotNull()
                .isEqualTo(defaultPageSize);
        assertThat(specificationPaginationConfig.getPageNumber()).isNotNull()
                .isEqualTo(defaultMinPageNumber);
        assertThat(specificationPaginationConfig.getMaxPageSize()).isNotNull()
                .isEqualTo(defaultMaxPageSize);
    }


}