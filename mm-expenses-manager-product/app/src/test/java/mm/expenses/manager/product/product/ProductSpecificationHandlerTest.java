package mm.expenses.manager.product.product;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.postgresql.specification.criteria.CriteriaParameter;
import mm.expenses.manager.common.postgresql.specification.criteria.FilteredField;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSpecificationHandlerTest {

    private static final List<String> selectedFields = new ArrayList<>();
    private static final List<String> filteredFields = new ArrayList<>();
    private static final List<String> sortedFields = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        val allFields = Arrays.stream(Product.class.getDeclaredFields()).toList();
        allFields.forEach(field -> {
            selectedFields.add(field.getName());

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
    }

    @Test
    void getSpecificationCriteria_shouldReturnSpecificationCriteria() {
        // given
        val handler = new ProductSpecificationHandler(new PaginationConfig());

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

        assertThat(sortableFields).isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(sortedFields)
                .containsExactlyInAnyOrderElementsOf(sortedFields);

        val pageable = specificationCriteria.getPagination();
        assertThat(pageable.getPageNumber()).isEqualTo(PaginationConfig.DEFAULT_PAGE_NUMBER);
        assertThat(pageable.getPageSize()).isEqualTo(PaginationConfig.DEFAULT_PAGE_SIZE);
    }

    @Test
    void additionalPredicateDefinition_shouldThrowSpecificationCriteriaException_whenParameterNameIsLongerThanExpected() {
        // given
        val handler = new ProductSpecificationHandler(new PaginationConfig());
        val criteriaParameter = new CriteriaParameter("price.value.test", FieldType.String, true, Operation.equal, List.of("0"));

        // when& then
        assertThatThrownBy(() -> handler.additionalPredicateDefinition().handle(new ArrayList<>(List.of(criteriaParameter)), null, null, null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(ProductExceptionMessage.PRODUCT_FILTERING_PATH_SIZE_EXCEEDED.getMessage());

    }

    @Test
    void additionalPredicateDefinition_shouldThrowSpecificationCriteriaException_whenParameterNameIsNotSupported() {
        // given
        val handler = new ProductSpecificationHandler(new PaginationConfig());

        val objectPropertyName = "price";
        val fieldPropertyName = "test";
        val criteriaParameter = new CriteriaParameter(String.format("%s.%s", objectPropertyName, fieldPropertyName), FieldType.String, true, Operation.equal, List.of("0"));

        // when& then
        assertThatThrownBy(() -> handler.additionalPredicateDefinition().handle(new ArrayList<>(List.of(criteriaParameter)), null, null, null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.FIELD_NOT_AVAILABLE_IN_OBJECT_FIELD_MESSAGE, objectPropertyName, fieldPropertyName));

    }

}