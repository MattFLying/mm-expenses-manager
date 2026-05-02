package mm.expenses.manager.order.processor;

import jakarta.persistence.Column;
import lombok.val;
import mm.expenses.manager.common.postgresql.specification.criteria.FilteredField;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.processor.search.OrderFilter;
import mm.expenses.manager.order.processor.search.OrderSpecificationHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSpecificationHandlerTest {

    private static final List<String> selectedFields = new ArrayList<>();
    private static final List<String> filteredFields = new ArrayList<>();
    private static final List<String> sortedFields = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        val allFields = Arrays.stream(Order.class.getDeclaredFields()).toList();
        allFields.forEach(field -> {
            val column = field.getAnnotation(Column.class);
            if (Objects.nonNull(column)) {
                selectedFields.add(field.getName());
            }

            val specificationDetails = field.getAnnotation(SpecificationDetailsAnnotation.class);
            if (Objects.nonNull(specificationDetails)) {
                if (specificationDetails.canBeFiltered()) {
                    filteredFields.add(field.getName());
                }
                if (specificationDetails.canBeSorted()) {
                    sortedFields.add(field.getName());
                }
            }
        });

        filteredFields.add(OrderFilter.PRODUCTS_COUNT_PROPERTY);
    }

    @Test
    void getSpecificationCriteria_shouldReturnSpecificationCriteria() {
        // given
        val handler = new OrderSpecificationHandler(new PaginationConfig());

        // when
        val specificationCriteria = handler.getSpecificationCriteria();

        val selectableFields = specificationCriteria.getSelectedFields();
        val filterableFields = specificationCriteria.getFilteredFields();
        val sortableFields = specificationCriteria.getSortedFields();

        // then
        assertThat(specificationCriteria).isNotNull();
        assertThat(specificationCriteria.getPagination()).isNotNull();

        assertThat(selectableFields).isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(selectedFields)
                .containsExactlyInAnyOrderElementsOf(selectedFields);

        assertThat(filterableFields).isNotNull()
                .isNotEmpty()
                .map(FilteredField::getName)
                .hasSameSizeAs(filteredFields)
                .containsExactlyInAnyOrderElementsOf(filteredFields);

        assertThat(filterableFields).map(FilteredField::getName)
                .contains(OrderFilter.PRODUCTS_COUNT_PROPERTY);

        assertThat(sortableFields).isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(sortedFields)
                .containsExactlyInAnyOrderElementsOf(sortedFields);

        val pageable = specificationCriteria.getPagination();
        assertThat(pageable.getPageNumber()).isEqualTo(PaginationConfig.DEFAULT_PAGE_NUMBER);
        assertThat(pageable.getPageSize()).isEqualTo(PaginationConfig.DEFAULT_PAGE_SIZE);
    }

}