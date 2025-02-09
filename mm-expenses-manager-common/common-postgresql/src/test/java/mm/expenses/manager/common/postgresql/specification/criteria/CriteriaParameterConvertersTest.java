package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.FieldTypeArgument;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.postgresql.specification.OperationArgument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriteriaParameterConvertersTest {

    private final CriteriaParameterConverters converters = new CriteriaParameterConverters();

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeWithAllOperationsArgument.class)
    void getConverter_shouldReturnConverterForEveryAvailableOperationAndFieldType(final FieldType fieldType, final Operation operation) {
        // given
        val fieldName = "testFieldName";
        val isJsonBField = false;
        val values = List.of();

        val criteriaParameter = new CriteriaParameter(fieldName, fieldType, isJsonBField, operation, values);

        // when
        val converter = converters.getConverter(criteriaParameter);

        // then
        assertThat(converter).isNotNull()
                .isInstanceOf(CriteriaParameterConverter.class);
    }

    @Test
    void getAvailableOperations_shouldReturnConverterForEveryAvailableOperationAndFieldType() {
        // given && when
        val availableConverters = converters.getAvailableOperations();

        // then
        assertThat(availableConverters).isNotNull()
                .isInstanceOf(Set.class)
                .isNotEmpty()
                .hasSameSizeAs(Operation.values());
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void getAvailableOperations_shouldContainsConverterForEveryOperation(final Operation operation) {
        // given && when
        val availableConverters = converters.getAvailableOperations();

        // then
        assertThat(availableConverters).isNotNull()
                .isInstanceOf(Set.class)
                .isNotEmpty()
                .hasSameSizeAs(Operation.values())
                .contains(operation);
    }

    @Test
    void getConverter_shouldThrowSpecificationCriteriaException_whenOperationContainsWorksWithUnsupportedFieldValueType() {
        // given
        val fieldName = "testFieldName";
        val isJsonBField = false;
        val fieldType = FieldType.String;
        val value = 5;
        val values = Collections.<Object>singletonList(value);
        val operation = Operation.contains;

        val criteriaParameter = new CriteriaParameter(fieldName, fieldType, isJsonBField, operation, values);
        val converter = converters.getConverter(criteriaParameter);

        // when & then
        assertThat(converter).isNotNull()
                .isInstanceOf(CriteriaParameterConverter.class);

        assertThatThrownBy(() -> converter.restrict(criteriaParameter, null, null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, operation, fieldName, fieldType.getType()));
    }

    @Test
    void getConverter_shouldThrowSpecificationCriteriaException_whenOperationEndsWithWorksWithUnsupportedFieldValueType() {
        // given
        val fieldName = "testFieldName";
        val isJsonBField = false;
        val fieldType = FieldType.String;
        val value = 10;
        val values = Collections.<Object>singletonList(value);
        val operation = Operation.endsWith;

        val criteriaParameter = new CriteriaParameter(fieldName, fieldType, isJsonBField, operation, values);
        val converter = converters.getConverter(criteriaParameter);

        // when & then
        assertThat(converter).isNotNull()
                .isInstanceOf(CriteriaParameterConverter.class);

        assertThatThrownBy(() -> converter.restrict(criteriaParameter, null, null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, operation, fieldName, fieldType.getType()));
    }

    @Test
    void getConverter_shouldThrowSpecificationCriteriaException_whenOperationStartsWithWorksWithUnsupportedFieldValueType() {
        // given
        val fieldName = "testFieldName";
        val isJsonBField = false;
        val fieldType = FieldType.String;
        val value = 12;
        val values = Collections.<Object>singletonList(value);
        val operation = Operation.startsWith;

        val criteriaParameter = new CriteriaParameter(fieldName, fieldType, isJsonBField, operation, values);
        val converter = converters.getConverter(criteriaParameter);

        // when & then
        assertThat(converter).isNotNull()
                .isInstanceOf(CriteriaParameterConverter.class);

        assertThatThrownBy(() -> converter.restrict(criteriaParameter, null, null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, operation, fieldName, fieldType.getType()));
    }

    @Test
    void getConverter_shouldThrowSpecificationCriteriaException_whenOperationIsNullWorksWithUnsupportedFieldValueType() {
        // given
        val fieldName = "testFieldName";
        val isJsonBField = false;
        val fieldType = FieldType.Boolean;
        val value = "notBoolean";
        val values = Collections.<Object>singletonList(value);
        val operation = Operation.isNull;

        val criteriaParameter = new CriteriaParameter(fieldName, fieldType, isJsonBField, operation, values);
        val converter = converters.getConverter(criteriaParameter);

        // when & then
        assertThat(converter).isNotNull()
                .isInstanceOf(CriteriaParameterConverter.class);

        assertThatThrownBy(() -> converter.restrict(criteriaParameter, null, null))
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, operation, fieldName, fieldType.getType()));
    }

}