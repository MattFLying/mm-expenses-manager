package mm.expenses.manager.common.postgresql.specification;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationParseException;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalCriteriaParameter;
import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.utils.pagination.sort.SortProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.Sort;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecificationHandlerTest {

    private static final String COLUMN_FIELD_NAME = "column";
    private static final String NAME_FIELD_NAME = "name";
    private static final String VALUE_FIELD_NAME = "value";
    private static final String TIME_FIELD_NAME = "time";

    @Test
    void getSpecificationCriteria_shouldReturnSpecificationCriteria() {
        // given
        val handler = new TestClassToHandlerSpecificationHandler();

        // when
        val specificationCriteria = handler.getSpecificationCriteria();

        val selectableFields = specificationCriteria.getSelectedFields();
        val filterableFields = specificationCriteria.getFilteredFields();
        val sortedFields = specificationCriteria.getSortedFields();

        // then
        assertThat(specificationCriteria).isNotNull();
        assertThat(specificationCriteria.getPagination()).isNull();

        assertThat(selectableFields).isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .containsExactlyInAnyOrder(COLUMN_FIELD_NAME);

        assertThat(sortedFields).isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .containsExactlyInAnyOrder(VALUE_FIELD_NAME);

        assertThat(filterableFields).isNotNull()
                .isNotEmpty()
                .hasSize(2);

        val filterableNameField = filterableFields.get(0);
        assertThat(filterableNameField).isNotNull();
        assertThat(filterableNameField.getName()).isEqualTo(NAME_FIELD_NAME);
        assertThat(filterableNameField.getType()).isEqualTo(FieldType.String);
        assertThat(filterableNameField.getIsJsonBType()).isFalse();

        val filterableColumnField = filterableFields.get(1);
        assertThat(filterableColumnField).isNotNull();
        assertThat(filterableColumnField.getName()).isEqualTo(COLUMN_FIELD_NAME);
        assertThat(filterableColumnField.getType()).isEqualTo(FieldType.String);
        assertThat(filterableColumnField.getIsJsonBType()).isTrue();
    }

    @Test
    void handle_shouldHandleDefaultPagination() {
        // given
        val handler = new TestClassToHandlerSpecificationHandler();

        // when
        val result = handler.handle(null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageable()).isNotNull();
        assertThat(result.specification()).isNotNull();

        val pageable = result.pageable();
        assertThat(pageable.getPageNumber()).isEqualTo(PaginationConfig.DEFAULT_PAGE_NUMBER);
        assertThat(pageable.getPageSize()).isEqualTo(PaginationConfig.DEFAULT_PAGE_SIZE);
        assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
    }

    @ParameterizedTest
    @EnumSource(SortProperty.SortDirection.class)
    void handle_shouldHandleCustomPagination(final SortProperty.SortDirection direction) {
        // given
        val pageNumber = 5;
        val pageSize = 100;
        val sortOrder = String.format("%s%s%s", VALUE_FIELD_NAME, Operation.SEPARATOR, direction);

        val params = new HashMap<String, String[]>();
        params.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(pageNumber)});
        params.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(pageSize)});
        params.put(PaginationConfig.SORT, new String[]{sortOrder});

        val handler = new TestClassToHandlerSpecificationHandler();

        // when
        val result = handler.handle(params, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageable()).isNotNull();
        assertThat(result.specification()).isNotNull();

        val pageable = result.pageable();
        assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);

        assertThat(pageable.getSort()).isNotNull();

        val sort = pageable.getSort();
        assertThat(sort.getOrderFor(VALUE_FIELD_NAME)).isNotNull();

        val order = sort.getOrderFor(VALUE_FIELD_NAME);
        assertThat(order).isNotNull();
        assertThat(order.getProperty()).isEqualTo(VALUE_FIELD_NAME);
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.fromString(direction.toLowerCase()));
    }

    @Test
    void handle_shouldHandleCustomPaginationWithDefaultDirectionASC() {
        // given
        val pageNumber = 5;
        val pageSize = 100;
        val sortOrder = String.format("%s", VALUE_FIELD_NAME);

        val params = new HashMap<String, String[]>();
        params.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(pageNumber)});
        params.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(pageSize)});
        params.put(PaginationConfig.SORT, new String[]{sortOrder});

        val handler = new TestClassToHandlerSpecificationHandler();

        // when
        val result = handler.handle(params, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageable()).isNotNull();
        assertThat(result.specification()).isNotNull();

        val pageable = result.pageable();
        assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);

        assertThat(pageable.getSort()).isNotNull();

        val sort = pageable.getSort();
        assertThat(sort.getOrderFor(VALUE_FIELD_NAME)).isNotNull();

        val order = sort.getOrderFor(VALUE_FIELD_NAME);
        assertThat(order).isNotNull();
        assertThat(order.getProperty()).isEqualTo(VALUE_FIELD_NAME);
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void handle_shouldHandleCustomPaginationWithLimitedPageSize() {
        // given
        val pageNumber = 1;
        val pageSize = 100;

        val params = new HashMap<String, String[]>();
        params.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(pageNumber)});
        params.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(pageSize)});

        val handler = new TestClassToHandlerSpecificationWithPagination();

        // when
        val result = handler.handle(params, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageable()).isNotNull();
        assertThat(result.specification()).isNotNull();

        val pageable = result.pageable();
        assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageable.getPageSize()).isEqualTo(PaginationConfig.DEFAULT_MAX_PAGE_SIZE);

        assertThat(pageable.getSort()).isNotNull();

        val sort = pageable.getSort();
        assertThat(sort.getOrderFor(VALUE_FIELD_NAME)).isNull();
    }

    @Test
    void handle_shouldCorrectlyHandleHiddenParameters() {
        // given
        val handler = new TestClassToHandlerSpecificationHandler();

        val hiddenCriteriaParameter_1 = AdditionalCriteriaParameter.of("testFieldName1", 1, Operation.greaterThan);
        val hiddenCriteriaParameter_2 = AdditionalCriteriaParameter.of("testFieldName2", "testFieldValue2", Operation.equal);
        val hiddenCriteriaParameter_3 = AdditionalCriteriaParameter.of("testFieldName3");

        // when
        val result = handler.handle(hiddenCriteriaParameter_1, hiddenCriteriaParameter_2, hiddenCriteriaParameter_3);

        // then
        assertThat(result).isNotNull();
        assertThat(result.pageable()).isNotNull();
        assertThat(result.specification()).isNotNull();
    }

    @Test
    void handle_shouldThrowSpecificationParseException_whenPageNumberHasIncorrectFormat() {
        // given
        val pageNumber = "test";
        val pageSize = 10;
        val sortOrder = String.format("%s%s%s", VALUE_FIELD_NAME, Operation.SEPARATOR, "desc");

        val params = new HashMap<String, String[]>();
        params.put(PaginationConfig.PAGE_NUMBER, new String[]{pageNumber});
        params.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(pageSize)});
        params.put(PaginationConfig.SORT, new String[]{sortOrder});

        val handler = new TestClassToHandlerSpecificationHandler();

        // when & then
        assertThatThrownBy(() -> handler.handle(params, null))
                .isInstanceOf(SpecificationParseException.class)
                .hasMessage(String.format(SpecificationParseException.INVALID_QUERIED_PARAMETER, PaginationConfig.PAGE_NUMBER, pageNumber));
    }

    @Test
    void handle_shouldThrowSpecificationParseException_whenPageSizeHasIncorrectFormat() {
        // given
        val pageNumber = 1;
        val pageSize = "test";
        val sortOrder = String.format("%s%s%s", VALUE_FIELD_NAME, Operation.SEPARATOR, "desc");

        val params = new HashMap<String, String[]>();
        params.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(pageNumber)});
        params.put(PaginationConfig.PAGE_SIZE, new String[]{pageSize});
        params.put(PaginationConfig.SORT, new String[]{sortOrder});

        val handler = new TestClassToHandlerSpecificationHandler();

        // when & then
        assertThatThrownBy(() -> handler.handle(params, null))
                .isInstanceOf(SpecificationParseException.class)
                .hasMessage(String.format(SpecificationParseException.INVALID_QUERIED_PARAMETER, PaginationConfig.PAGE_SIZE, pageSize));
    }

    @Test
    void handle_shouldThrowSpecificationParseException_whenFieldCannotBeSorted() {
        // given
        val pageNumber = 1;
        val pageSize = 10;
        val sortOrder = String.format("%s%s%s", TIME_FIELD_NAME, Operation.SEPARATOR, "asc");

        val params = new HashMap<String, String[]>();
        params.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(pageNumber)});
        params.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(pageSize)});
        params.put(PaginationConfig.SORT, new String[]{sortOrder});

        val handler = new TestClassToHandlerSpecificationHandler();

        // when & then
        assertThatThrownBy(() -> handler.handle(params, null))
                .isInstanceOf(SpecificationParseException.class)
                .hasMessage(String.format(SpecificationParseException.CANNOT_SORT_BY_FIELD, TIME_FIELD_NAME));
    }

    private static class TestClassToHandlerSpecificationHandler extends SpecificationHandler<TestClassToHandlerSpecification> {

        protected final SpecificationCriteria specificationCriteria;

        public TestClassToHandlerSpecificationHandler() {
            this.specificationCriteria = SpecificationScanner.scan(TestClassToHandlerSpecification.class);
        }

        @Override
        public SpecificationCriteria getSpecificationCriteria() {
            return specificationCriteria;
        }

    }

    private static class TestClassToHandlerSpecificationWithPagination extends TestClassToHandlerSpecificationHandler {

        public TestClassToHandlerSpecificationWithPagination() {
            super();
        }

        @Override
        public SpecificationCriteria getSpecificationCriteria() {
            specificationCriteria.setPagination(new PaginationConfig());

            return specificationCriteria;
        }

    }

}