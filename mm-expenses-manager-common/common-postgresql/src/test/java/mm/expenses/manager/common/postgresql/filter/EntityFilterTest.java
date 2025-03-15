package mm.expenses.manager.common.postgresql.filter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static mm.expenses.manager.common.postgresql.filter.EntityFilter.IS_DELETED_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityFilterTest {

    @Test
    void isExplicitQueryOriented_shouldReturnFalse_whenExplicitQueryIsNotUsed() {
        // given
        val filter = TestFilter.builder().build();

        // when
        val isExplicitQueryOriented = filter.isExplicitQueryOriented();

        // then
        assertThat(isExplicitQueryOriented).isFalse();
    }

    @Test
    void isExplicitQueryOriented_shouldReturnTrue_whenExplicitQueryIsUsed() {
        // given
        val testValueQuery = "testValue";
        val filter = TestFilter.builder().query(testValueQuery).testValue(testValueQuery).build();

        // when
        val isExplicitQueryOriented = filter.isExplicitQueryOriented();

        // then
        assertThat(isExplicitQueryOriented).isTrue();
    }

    @Test
    void isExplicitQueryOriented_shouldThrowApiBadRequestException_whenListOfSpecificQueryParametersHaveDefinedParam() {
        // given
        val testValueQuery = "testValue";
        val filter = TestFilter.builder().query(testValueQuery).build();

        // given & when & then
        assertThatThrownBy(filter::isExplicitQueryOriented)
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY);
    }

    @Test
    void buildPagination_shouldAddPaginationQueryParametersBasedOnPaginationInput() {
        // given
        val pageNumber = 0;
        val pageSize = 10;
        val filter = TestFilter.builder().paginationConfig(PageRequest.of(pageNumber, pageSize)).build();

        val queryParameters = new HashMap<String, String[]>();

        // when
        filter.buildPagination(queryParameters);

        // then
        assertThat(queryParameters).isNotNull().isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(
                                PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(pageNumber)},
                                PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(pageSize)}
                        )
                );
    }

    @ParameterizedTest
    @ArgumentsSource(BooleanArgument.class)
    void buildDeleted_shouldAddDeletedQueryParameterBasedOnDeletedInput(final Boolean isDeleted) {
        // given
        val filter = TestFilter.builder().isDeleted(isDeleted).build();

        val queryParameters = new HashMap<String, String[]>();

        // when
        filter.buildDeleted(queryParameters);

        // then
        if (isDeleted) {
            assertThat(queryParameters).isNotNull().isNotEmpty()
                    .hasSize(1)
                    .containsExactlyInAnyOrderEntriesOf(Map.of(IS_DELETED_PROPERTY, new String[]{String.valueOf(isDeleted)}));
        } else {
            assertThat(queryParameters).isNotNull().isEmpty();
        }
    }

    @SuperBuilder
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private static class TestFilter extends EntityFilter {

        private final String testValue;

        @Override
        protected List<Boolean> getSpecificFiltersPresenceList() {
            if (Objects.nonNull(getQuery()) && Objects.nonNull(testValue)) {
                return List.of();
            }
            if (Objects.nonNull(testValue)) {
                return List.of(true);
            }
            if (Objects.nonNull(getQuery())) {
                return List.of(true);
            }
            return List.of();
        }

        @Override
        protected void buildSpecificQueryParameters(Map<String, String[]> queryParameters) {

        }

    }

    private static class BooleanArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(false), Arguments.of(true));
        }

    }

}