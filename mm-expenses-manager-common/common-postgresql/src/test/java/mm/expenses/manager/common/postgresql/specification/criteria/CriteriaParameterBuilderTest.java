package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.FieldTypeArgument;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.postgresql.specification.OperationArgument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriteriaParameterBuilderTest {

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void build_shouldBuildCriteriaParameter_withDefaultOperationEqual(final FieldType type) {
        // given
        val nameOfFilteredField = "testFieldName";
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getValueByFieldType(type);
        val map = Map.of(nameOfFilteredField, String.valueOf(fieldValue));

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when
        val result = builder.build();

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull()
                .isEqualTo(nameOfFilteredField);
        assertThat(result.fieldType()).isNotNull()
                .isEqualTo(type);
        assertThat(result.operation()).isNotNull()
                .isEqualTo(Operation.equal);
        assertThat(result.values()).isNotNull()
                .isInstanceOf(List.class)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(fieldValue);
        assertThat(result.isJsonBField()).isFalse();
        assertThat(result.isStandard()).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void build_shouldBuildCriteriaParameter_asNonStandardProperty(final FieldType type) {
        // given
        val nameOfFilteredField = "testFieldName";
        val filteredField = FilteredField.of(nameOfFilteredField, type, false);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getValueByFieldType(type);
        val map = Map.of(nameOfFilteredField, String.valueOf(fieldValue));

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when
        val result = builder.build();

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull()
                .isEqualTo(nameOfFilteredField);
        assertThat(result.fieldType()).isNotNull()
                .isEqualTo(type);
        assertThat(result.operation()).isNotNull()
                .isEqualTo(Operation.equal);
        assertThat(result.values()).isNotNull()
                .isInstanceOf(List.class)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(fieldValue);
        assertThat(result.isJsonBField()).isFalse();
        assertThat(result.isStandard()).isFalse();
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void build_shouldThrowRuntimeException_whenFieldOfGivenNameIsNotFilterable(final FieldType type) {
        // given
        val nameOfNotExistingField = "notExistingFieldName";
        val nameOfFilteredField = "testFieldName";
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getValueByFieldType(type);
        val map = Map.of(nameOfNotExistingField, String.valueOf(fieldValue));

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.INCORRECT_FILTERABLE_FIELD_NAME_MESSAGE, nameOfNotExistingField));
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeWithAvailableOperationsArgument.class)
    void build_shouldBuildCriteriaParameter_withAvailableOperations(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getFieldValueByFieldTypeExceptIsNullOperation(type, operation);
        val map = createMapOfFilteredFieldToCriteria(type, nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when
        val result = builder.build();

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull()
                .isEqualTo(nameOfFilteredField);
        assertThat(result.fieldType()).isNotNull()
                .isEqualTo(type);
        assertThat(result.operation()).isNotNull()
                .isEqualTo(operation);
        assertThat(result.isJsonBField()).isFalse();
        assertThat(result.isStandard()).isTrue();

        assertThat(result.values()).isNotNull()
                .isInstanceOf(List.class)
                .isNotEmpty()
                .hasSize(1);
        if (FieldType.List.equals(type) && Operation.isNull.equals(operation)) {
            assertThat(result.values()).containsExactly(fieldValue);
        } else if (FieldType.List.equals(type)) {
            assertThat(result.values()).containsExactly(map.get(nameOfFilteredFieldWithOperator));
        } else {
            assertThat(result.values()).containsExactly(fieldValue);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.OperationExceptEqualArgument.class)
    void build_shouldThrowRuntimeException_whenOperationDifferentThanEqualPassedForBooleanField(final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val type = FieldType.Boolean;
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getValueByFieldType(type);
        val map = Map.of(nameOfFilteredFieldWithOperator, String.valueOf(fieldValue));

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_IS_UNAVAILABLE_FOR_BOOLEAN_FIELD_MESSAGE, operation, nameOfFilteredField));
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeDifferentThanStringWithStringSpecificOperationsArgument.class)
    void build_shouldThrowRuntimeException_whenNonStringFieldWithStringSpecificOperationsPassed(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getValueByFieldType(type);
        val map = Map.of(nameOfFilteredFieldWithOperator, String.valueOf(fieldValue));

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_IS_UNAVAILABLE_FOR_NOT_TEXT_FIELD_MESSAGE, operation, nameOfFilteredField));
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeWithAvailableOperationsArgument.class)
    void build_shouldThrowRuntimeException_whenMoreThanOneOperatorPassed(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s:%s", nameOfFilteredField, operation, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = getFieldValueByFieldTypeExceptIsNullOperation(type, operation);
        val map = createMapOfFilteredFieldToCriteria(type, nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(SpecificationCriteriaException.ONLY_ONE_OPERATION_ALLOWED_MESSAGE);
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeWithAvailableIsNullOperationArgument.class)
    void build_shouldThrowRuntimeException_whenIsNullOperationHasIncorrectBooleanValue(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = "";
        val map = createMapOfFilteredFieldToCriteria(type, nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_REQUIRES_BOOLEAN_VALUE_FOR_FIELD_MESSAGE, operation, nameOfFilteredField));
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeListWithAvailableOperationsArgument.class)
    void build_shouldThrowRuntimeException_whenEmptyListPassed(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = "";
        val map = createMapOfFilteredFieldToCriteria(type, nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.EMPTY_LIST_NOT_ALLOWED_FOR_FIELD_MESSAGE, nameOfFilteredField));
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeListWithEqualOperationsArgument.class)
    void build_shouldThrowRuntimeException_whenMoreThanOneValuePassed(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = "value1,value2";
        val map = createMapOfFilteredFieldToCriteria(type, nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.OPERATION_REQUIRES_ONLY_ONE_VALUE_MESSAGE, operation));
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.FieldTypeWithAvailableOperationsArgument.class)
    void build_shouldThrowRuntimeException_whenEmptyFieldValuePassed(final FieldType type, final Operation operation) {
        // given
        val nameOfFilteredField = "testFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, type);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = "";
        val map = createMapOfFilteredFieldToCriteria(type, nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        if (Operation.isNull.equals(operation)) {
            assertThatThrownBy(builder::build)
                    .isInstanceOf(SpecificationCriteriaException.class)
                    .hasMessage(String.format(SpecificationCriteriaException.OPERATION_REQUIRES_BOOLEAN_VALUE_FOR_FIELD_MESSAGE, operation, nameOfFilteredField));
        } else if (FieldType.List.equals(type)) {
            assertThatThrownBy(builder::build)
                    .isInstanceOf(SpecificationCriteriaException.class)
                    .hasMessage(String.format(SpecificationCriteriaException.EMPTY_LIST_NOT_ALLOWED_FOR_FIELD_MESSAGE, nameOfFilteredField));
        } else {
            assertThatThrownBy(builder::build)
                    .isInstanceOf(SpecificationCriteriaException.class)
                    .hasMessage(String.format(SpecificationCriteriaException.EMPTY_VALUE_FOR_FIELD_OF_TYPE_MESSAGE, nameOfFilteredField, type));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(BooleanValueArgument.class)
    void build_shouldParseBooleanValue(final String fieldValue) {
        // given
        val booleanValues = Map.of(
                "true", true,
                "false", false,
                "1", true,
                "0", false
        );

        val operation = Operation.isNull;
        val nameOfFilteredField = "testBooleanFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, FieldType.String);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val map = Map.of(nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when
        val result = builder.build();

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isNotNull()
                .isEqualTo(nameOfFilteredField);
        assertThat(result.fieldType()).isNotNull()
                .isEqualTo(filteredField.getType());
        assertThat(result.operation()).isNotNull()
                .isEqualTo(operation);
        assertThat(result.values()).isNotNull()
                .isInstanceOf(List.class)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(booleanValues.get(fieldValue));
        assertThat(result.isJsonBField()).isFalse();
        assertThat(result.isStandard()).isTrue();
    }

    @Test
    void build_shouldThrowSpecificationCriteriaException_whenNotBooleanRelatedValuePassed() {
        // given
        val operation = Operation.isNull;
        val nameOfFilteredField = "testBooleanFieldName";
        val nameOfFilteredFieldWithOperator = String.format("%s:%s", nameOfFilteredField, operation);
        val filteredField = FilteredField.of(nameOfFilteredField, FieldType.String);

        val filteredFields = List.of(filteredField);
        val specificationCriteria = SpecificationCriteria.of(filteredFields, null, null);

        val fieldValue = "11";
        val map = Map.of(nameOfFilteredFieldWithOperator, fieldValue);

        val builder = new CriteriaParameter.CriteriaParameterBuilder(map.entrySet().stream().findFirst().orElse(null), specificationCriteria);

        // when & then
        assertThatThrownBy(builder::build)
                .isInstanceOf(SpecificationCriteriaException.class)
                .hasMessage(String.format(SpecificationCriteriaException.BOOLEAN_INVALID_VALUE_MESSAGE, "true", "1", "false", "0"));
    }

    private Map<String, String> createMapOfFilteredFieldToCriteria(final FieldType type, final String nameOfFilteredFieldWithOperator, final Object fieldValue) {
        if (FieldType.List.equals(type)) {
            if (fieldValue instanceof Boolean boolValue) {
                return Map.of(nameOfFilteredFieldWithOperator, String.valueOf(boolValue));
            }
            try {
                return Map.of(nameOfFilteredFieldWithOperator, String.format("\"%s\"", ((List) fieldValue).get(0)));
            } catch (ClassCastException exception) {
                return Map.of(nameOfFilteredFieldWithOperator, String.format("%s", List.of(fieldValue)));
            }
        }
        return Map.of(nameOfFilteredFieldWithOperator, String.valueOf(fieldValue));
    }

    private Object getFieldValueByFieldTypeExceptIsNullOperation(final FieldType type, final Operation operation) {
        var fieldValue = FieldType.valueOf(getValueByFieldType(type).toString(), type);
        if (Operation.isNull.equals(operation)) {
            fieldValue = false;
        }
        return fieldValue;
    }

    private Object getValueByFieldType(final FieldType type) {
        if (FieldType.Boolean.equals(type)) {
            return true;
        } else if (FieldType.Long.equals(type)) {
            return 10L;
        } else if (FieldType.Integer.equals(type)) {
            return 15;
        } else if (FieldType.Instant.equals(type)) {
            return Instant.now();
        } else {
            return "test value";
        }
    }

    private static class BooleanValueArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of("true", "1", "false", "0").map(Arguments::of);
        }

    }

}