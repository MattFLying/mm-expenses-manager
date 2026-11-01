package mm.expenses.manager.account.processor.search;

import jakarta.persistence.Column;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.postgresql.specification.criteria.FilteredField;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class UserSpecificationHandlerTest {

    private static final List<String> selectedFields = new ArrayList<>();
    private static final List<String> filteredFields = new ArrayList<>();
    private static final List<String> sortedFields = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        final var allFields = Arrays.stream(User.class.getDeclaredFields()).toList();
        allFields.forEach(field -> {
            final var column = field.getAnnotation(Column.class);
            if (Objects.nonNull(column)) {
                selectedFields.add(field.getName());
            }

            final var specificationDetails = field.getAnnotation(SpecificationDetailsAnnotation.class);
            if (Objects.nonNull(specificationDetails)) {
                if (specificationDetails.canBeFiltered()) {
                    filteredFields.add(field.getName());
                }
                if (specificationDetails.canBeSorted()) {
                    sortedFields.add(field.getName());
                }
            }
        });
    }

    @Test
    void getSpecificationCriteria_shouldReturnSpecificationCriteria() {
        // given
        final var handler = new UserSpecificationHandler(new PaginationConfig());

        // when
        final var specificationCriteria = handler.getSpecificationCriteria();

        final var selectableFields = specificationCriteria.getSelectedFields();
        final var filterableFields = specificationCriteria.getFilteredFields();
        final var sortableFields = specificationCriteria.getSortedFields();

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

        assertThat(sortableFields).isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(sortedFields)
                .containsExactlyInAnyOrderElementsOf(sortedFields);

        final var pageable = specificationCriteria.getPagination();
        assertThat(pageable.getPageNumber()).isEqualTo(PaginationConfig.DEFAULT_PAGE_NUMBER);
        assertThat(pageable.getPageSize()).isEqualTo(PaginationConfig.DEFAULT_PAGE_SIZE);
    }

}