package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.val;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.FieldTypeArgument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilteredFieldTest {

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void validateValues_shouldThrowNPE_whenFilteredFieldValueIsNull(final FieldType type) {
        // given
        val name = "";
        val isJsonBType = false;

        val filteredField = new FilteredField(name, type, isJsonBType);

        // when & then
        assertThatThrownBy(() -> filteredField.validateValues(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Filtered field value cannot be null");
    }

    @Test
    void validateValues_shouldValidateValueAsString() {
        // given
        val name = "";
        val isJsonBType = false;
        val value = "test";

        val filteredField = new FilteredField(name, FieldType.String, isJsonBType);

        // when
        val result = filteredField.validateValues(value);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(value);
    }

    @Test
    void validateValues_shouldValidateValueAsInteger() {
        // given
        val name = "";
        val isJsonBType = false;
        val value = "1";

        val filteredField = new FilteredField(name, FieldType.Integer, isJsonBType);

        // when
        val result = filteredField.validateValues(value);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(Integer.valueOf(value));
    }

    @Test
    void validateValues_shouldValidateValueAsLong() {
        // given
        val name = "";
        val isJsonBType = false;
        val value = "1";

        val filteredField = new FilteredField(name, FieldType.Long, isJsonBType);

        // when
        val result = filteredField.validateValues(value);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(Long.valueOf(value));
    }

    @Test
    void validateValues_shouldValidateValueAsBoolean() {
        // given
        val name = "";
        val isJsonBType = false;
        val value = "true";

        val filteredField = new FilteredField(name, FieldType.Boolean, isJsonBType);

        // when
        val result = filteredField.validateValues(value);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(Boolean.valueOf(value));
    }

    @Test
    void validateValues_shouldValidateValueAsListFromArray() {
        // given
        val name = "";
        val isJsonBType = false;
        val valueAsString = "testList";
        val value = new String[]{valueAsString};

        val filteredField = new FilteredField(name, FieldType.List, isJsonBType);

        // when
        val result = filteredField.validateValues(valueAsString);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(value);
    }

    @Test
    void validateValues_shouldValidateValueAsListFromCollection() {
        // given
        val name = "";
        val isJsonBType = false;
        val valueAsString = "testList";

        val filteredField = new FilteredField(name, FieldType.List, isJsonBType);

        // when
        val result = filteredField.validateValues(valueAsString);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(valueAsString);
    }

    @Test
    void validateValues_shouldValidateValueAsInstant() {
        // given
        val name = "";
        val isJsonBType = false;
        val instant = Instant.now();
        val value = instant.toString();

        val filteredField = new FilteredField(name, FieldType.Instant, isJsonBType);

        // when
        val result = filteredField.validateValues(value);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(instant);
    }

    @Test
    void hasSameName_shouldReturnTrue_whenComparedWithTheSameName() {
        // given
        val name = "textFieldName";
        val isJsonBType = false;

        val filteredField = new FilteredField(name, FieldType.String, isJsonBType);

        // when
        val result = filteredField.hasSameName(name);

        // then
        assertThat(result).isNotNull()
                .isTrue();
    }

    @Test
    void hasSameName_shouldReturnTrue_whenComparedWithWithSameNameAsAlternativeName() {
        // given
        val name = "textFieldName";
        val alternativeName = "alternativeFieldName";
        val isJsonBType = false;

        val filteredField = new FilteredField(name, alternativeName, FieldType.String, isJsonBType);

        // when
        val result = filteredField.hasSameName(alternativeName);

        // then
        assertThat(result).isNotNull()
                .isTrue();
    }

    @Test
    void hasSameName_shouldReturnFalse_whenComparedWithDifferentName() {
        // given
        val name = "textFieldName";
        val isJsonBType = false;
        val nameToCompare = "nameToCompare";

        val filteredField = new FilteredField(name, FieldType.String, isJsonBType);

        // when
        val result = filteredField.hasSameName(nameToCompare);

        // then
        assertThat(result).isNotNull()
                .isFalse();
    }

    @Test
    void hasSameName_shouldReturnFalse_whenComparedWithWithDifferentNameThanEvenAlternativeName() {
        // given
        val name = "textFieldName";
        val alternativeName = "alternativeFieldName";
        val isJsonBType = false;
        val nameToCompare = "nameToCompare";

        val filteredField = new FilteredField(name, alternativeName, FieldType.String, isJsonBType);

        // when
        val result = filteredField.hasSameName(nameToCompare);

        // then
        assertThat(result).isNotNull()
                .isFalse();
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void of_shouldThrowNPE_whenNullNamePassed(final FieldType type) {
        // given & when & then
        assertThatThrownBy(() -> FilteredField.of(null, type))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Field name cannot be null");
    }

    @Test
    void of_shouldThrowNPE_whenNullNamePassed() {
        // given
        val name = "name";

        // when & then
        assertThatThrownBy(() -> FilteredField.of(name, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Field fieldType cannot be null");
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void of_shouldCreateFilteredFieldWithDefaultJsonBfieldAsFalse(final FieldType type) {
        // given
        val name = "name";

        // when
        val result = FilteredField.of(name, type);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(FilteredField.class);

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getAlternativeName()).isNull();
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getIsJsonBType()).isFalse();
    }

    @Test
    void of_shouldThrowNPE_whenNullAlternativeNamePassed() {
        // given
        val name = "name";

        // when & then
        assertThatThrownBy(() -> FilteredField.of(name, null, FieldType.String))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Alternative field name cannot be null");
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void of_shouldCreateFilteredFieldWithAlternativeName(final FieldType type) {
        // given
        val name = "name";
        val alternativeName = "alternativeName";

        // when
        val result = FilteredField.of(name, alternativeName, type);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(FilteredField.class);

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getAlternativeName()).isEqualTo(alternativeName);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getIsJsonBType()).isFalse();
    }

}