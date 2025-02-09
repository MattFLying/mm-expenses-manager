package mm.expenses.manager.common.postgresql.specification;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldTypeTest {

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void getName_shouldReturnCorrectName(final FieldType type) {
        // given & when
        val result = type.getName();

        // then
        assertThat(result).isNotNull()
                .isEqualTo(type.name());
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void type_shouldReturnCorrectType(final FieldType type) {
        // given & when
        val result = type.getType();

        // then
        assertThat(result).isNotNull()
                .isEqualTo(type.getType());
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void type_shouldReturnCorrectSimpleType(final FieldType type) {
        // given & when
        val result = type.getSimpleName();

        // then
        assertThat(result).isNotNull()
                .isEqualTo(type.getSimpleName());
    }

    @Test
    void of_shouldThrowNPE_whenNullFieldPassed() {
        // given & when & then
        assertThatThrownBy(() -> FieldType.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Field cannot be null");
    }

    @Test
    void of_field_shouldIdentifyBooleanField() throws NoSuchFieldException {
        // given
        val fieldName = "booleanField";
        val testClass = new TestFieldType();
        testClass.setBooleanField(true);

        val booleanField = Arrays.stream(testClass.getClass().getDeclaredFields())
                .filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElseThrow(() -> noSuchFieldException(fieldName));

        // when
        val result = FieldType.of(booleanField);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull()
                .isAssignableFrom(Boolean.class);
        assertThat(result.getSimpleName()).isEqualTo(FieldType.Boolean.getSimpleName());
        assertThat(result.getName()).isEqualTo(FieldType.Boolean.name());
    }

    @Test
    void of_field_shouldIdentifyStringField() throws NoSuchFieldException {
        // given
        val fieldName = "stringField";
        val testClass = new TestFieldType();
        testClass.setStringField("test");

        val stringField = Arrays.stream(testClass.getClass().getDeclaredFields())
                .filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElseThrow(() -> noSuchFieldException(fieldName));

        // when
        val result = FieldType.of(stringField);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull()
                .isAssignableFrom(String.class);
        assertThat(result.getSimpleName()).isEqualTo(FieldType.String.getSimpleName());
        assertThat(result.getName()).isEqualTo(FieldType.String.name());
    }

    @Test
    void of_field_shouldIdentifyIntegerField() throws NoSuchFieldException {
        // given
        val fieldName = "integerField";
        val testClass = new TestFieldType();
        testClass.setIntegerField(5);

        val integerField = Arrays.stream(testClass.getClass().getDeclaredFields())
                .filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElseThrow(() -> noSuchFieldException(fieldName));

        // when
        val result = FieldType.of(integerField);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull()
                .isAssignableFrom(Integer.class);
        assertThat(result.getSimpleName()).isEqualTo(FieldType.Integer.getSimpleName());
        assertThat(result.getName()).isEqualTo(FieldType.Integer.name());
    }

    @Test
    void of_field_shouldIdentifyLongField() throws NoSuchFieldException {
        // given
        val fieldName = "longField";
        val testClass = new TestFieldType();
        testClass.setLongField(10L);

        val longField = Arrays.stream(testClass.getClass().getDeclaredFields())
                .filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElseThrow(() -> noSuchFieldException(fieldName));

        // when
        val result = FieldType.of(longField);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull()
                .isAssignableFrom(Long.class);
        assertThat(result.getSimpleName()).isEqualTo(FieldType.Long.getSimpleName());
        assertThat(result.getName()).isEqualTo(FieldType.Long.name());
    }

    @Test
    void of_field_shouldIdentifyInstantField() throws NoSuchFieldException {
        // given
        val fieldName = "instantField";
        val testClass = new TestFieldType();
        testClass.setInstantField(Instant.now());

        val instantField = Arrays.stream(testClass.getClass().getDeclaredFields())
                .filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElseThrow(() -> noSuchFieldException(fieldName));

        // when
        val result = FieldType.of(instantField);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull()
                .isAssignableFrom(Instant.class);
        assertThat(result.getSimpleName()).isEqualTo(FieldType.Instant.getSimpleName());
        assertThat(result.getName()).isEqualTo(FieldType.Instant.name());
    }

    @Test
    void of_field_shouldIdentifyListField() throws NoSuchFieldException {
        // given
        val fieldName = "listField";
        val testClass = new TestFieldType();
        testClass.setListField(List.of());

        val listField = Arrays.stream(testClass.getClass().getDeclaredFields())
                .filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElseThrow(() -> noSuchFieldException(fieldName));

        // when
        val result = FieldType.of(listField);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull()
                .isAssignableFrom(List.class);
        assertThat(result.getSimpleName()).isEqualTo(FieldType.List.getSimpleName());
        assertThat(result.getName()).isEqualTo(FieldType.List.name());
    }

    @Test
    void valueOf_valueExpectedType_shouldThrowNPE_whenNullAsExpectedFieldPassed() {
        // given & when & then
        assertThatThrownBy(() -> FieldType.valueOf("", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expected fieldType cannot be null");
    }

    @Test
    void valueOf_valueExpectedType_shouldMapBooleanField() {
        // given
        val testClass = new TestFieldType();
        testClass.setBooleanField(true);

        val booleanAsString = String.valueOf(testClass.getBooleanField());

        // when
        val result = FieldType.valueOf(booleanAsString, FieldType.Boolean);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(Boolean.class);

        val boolResult = (Boolean) result;
        assertThat(boolResult).isEqualTo(testClass.getBooleanField());
    }

    @Test
    void valueOf_valueExpectedType_shouldMapStringField() {
        // given
        val testClass = new TestFieldType();
        testClass.setStringField("test");

        val asString = String.valueOf(testClass.getStringField());

        // when
        val result = FieldType.valueOf(asString, FieldType.String);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(String.class);

        val stringResult = (String) result;
        assertThat(stringResult).isEqualTo(testClass.getStringField());
    }

    @Test
    void valueOf_valueExpectedType_shouldMapIntegerField() {
        // given
        val testClass = new TestFieldType();
        testClass.setIntegerField(15);

        val asInteger = String.valueOf(testClass.getIntegerField());

        // when
        val result = FieldType.valueOf(asInteger, FieldType.Integer);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(Integer.class);

        val integerResult = (Integer) result;
        assertThat(integerResult).isEqualTo(testClass.getIntegerField());
    }

    @Test
    void valueOf_valueExpectedType_shouldMapLongField() {
        // given
        val testClass = new TestFieldType();
        testClass.setLongField(13L);

        val asLong = String.valueOf(testClass.getLongField());

        // when
        val result = FieldType.valueOf(asLong, FieldType.Long);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(Long.class);

        val longResult = (Long) result;
        assertThat(longResult).isEqualTo(testClass.getLongField());
    }

    @Test
    void valueOf_valueExpectedType_shouldMapInstantField() {
        // given
        val testClass = new TestFieldType();
        testClass.setInstantField(Instant.now());

        val asInstant = String.valueOf(testClass.getInstantField());

        // when
        val result = FieldType.valueOf(asInstant, FieldType.Instant);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(Instant.class);

        val instantResult = (Instant) result;
        assertThat(instantResult).isEqualTo(testClass.getInstantField());
    }

    @Test
    void valueOf_valueExpectedType_shouldMapListField() {
        // given
        val testClass = new TestFieldType();
        testClass.setListField(List.of());

        val asList = String.valueOf(testClass.getListField());

        // when
        val result = FieldType.valueOf(asList, FieldType.List);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class);

        val listResult = (List<?>) result;
        assertThat(listResult).isEqualTo(testClass.getListField());
    }

    @Test
    void valueOf_valueExpectedType_shouldMapListAsStringToListField() {
        // given
        val firstValue = 1;
        val secondValue = 2;
        val listAsString = String.format("[%s, %s]", firstValue, secondValue);

        // when
        val result = FieldType.valueOf(listAsString, FieldType.List);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class);

        val listResult = (List<Integer>) result;
        assertThat(listResult).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(2)
                .containsExactly(firstValue, secondValue);
    }

    @Test
    void valueOf_valueExpectedType_shouldMapTextWhichIsNotAListToListField() {
        // given
        val firstValue = 1;
        val secondValue = 2;
        val listAsString = String.format("%s, %s", firstValue, secondValue);

        // when
        val result = FieldType.valueOf(listAsString, FieldType.List);

        // then
        assertThat(result).isNotNull()
                .isInstanceOf(List.class);

        val listResult = (List<String>) result;
        assertThat(listResult).isNotNull()
                .isInstanceOf(List.class)
                .hasSize(1)
                .containsExactly(listAsString);
    }

    @ParameterizedTest
    @ArgumentsSource(FieldTypeArgument.class)
    void valueOf_valueExpectedType_shouldThrowNPE_whenNullAsValuePassed(final FieldType type) {
        // given & when & then
        assertThatThrownBy(() -> FieldType.valueOf(null, type))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Value cannot be null");
    }

    private NoSuchFieldException noSuchFieldException(final String fieldName) {
        return new NoSuchFieldException(String.format("Field %s does not exists.", fieldName));
    }

    @Getter
    @Setter
    private static class TestFieldType {

        private Boolean booleanField;
        private String stringField;
        private Integer integerField;
        private Long longField;
        private Instant instantField;
        private List<Object> listField;

    }

}