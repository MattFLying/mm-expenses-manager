package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.val;
import mm.expenses.manager.common.postgresql.specification.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdditionalCriteriaParameterTest {

    @Test
    void of_shouldThrowNPE_whenNullNamePassed() {
        // given & when & then
        assertThatThrownBy(() -> AdditionalCriteriaParameter.of(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Param name cannot be null");
    }

    @Test
    void of_shouldThrowNPE_whenNullValuePassed() {
        // given
        val name = "testName";

        // when & then
        assertThatThrownBy(() -> AdditionalCriteriaParameter.of(name, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Param value cannot be null");
    }

    @Test
    void of_shouldReturnHiddenCriteriaParameterWithNullOperation() {
        // given
        val name = "testName";
        val value = "testValue";

        // when & then
        val result = AdditionalCriteriaParameter.of(name, value);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getOperation()).isNull();
    }

    @Test
    void of_shouldThrowNPE_whenNullOperationPassed() {
        // given
        val name = "testName";
        val value = "testValue";

        // when & then
        assertThatThrownBy(() -> AdditionalCriteriaParameter.of(name, value, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Param operation cannot be null");
    }

    @Test
    void of_shouldReturnHiddenCriteriaParameterWithNullOperationAndValue() {
        // given
        val name = "testName";

        // when & then
        val result = AdditionalCriteriaParameter.of(name);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getValue()).isNull();
        assertThat(result.getOperation()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void of_shouldReturnHiddenCriteriaParameterWithNullOperation(final Operation type) {
        // given
        val name = "testName";
        val value = "testValue";

        // when & then
        val result = AdditionalCriteriaParameter.of(name, value, type);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getOperation()).isEqualTo(type);
    }

}