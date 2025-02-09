package mm.expenses.manager.common.postgresql.specification;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.OperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationTest {

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void getValueWithSeparator_shouldReturnCorrectValueWithSeparator(final Operation type) {
        // given & when
        val result = type.getValueWithSeparator();

        // then
        assertThat(result).isNotNull()
                .isEqualTo(String.format("%s%s", Operation.SEPARATOR, type.name()));
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void isIn_shouldThrowNPE_whenNullOperationsPassed(final Operation type) {
        // given & when & then
        assertThatThrownBy(() -> type.isIn(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expected operations cannot be null");
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void isIn_shouldReturnFalse_whenTypeIsNotInTheExpectedOperations(final Operation type) {
        // given
        val isInValues = List.of(randomOperation(type));

        // when
        val result = type.isIn(isInValues.toArray(new Operation[]{}));

        // then
        assertThat(result).isNotNull().isFalse();
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void isIn_shouldReturnTrue_whenTypeIsInTheExpectedOperations(final Operation type) {
        // given
        val isInValues = List.of(type, randomOperation(type));

        // when
        val result = type.isIn(isInValues.toArray(new Operation[]{}));

        // then
        assertThat(result).isNotNull().isTrue();
    }

    @Test
    void of_shouldReturnEqualOperation_whenPassedValueIsNull() {
        // given & when
        val result = Operation.of(null);

        // then
        assertThat(result).isNotNull()
                .isEqualTo(Operation.equal);
    }

    @ParameterizedTest
    @ArgumentsSource(OperationArgument.class)
    void of_shouldReturnTrue_whenTypeIsInTheExpectedOperations(final Operation type) {
        // given & when
        val result = Operation.of(type);

        // then
        assertThat(result).isNotNull()
                .isEqualTo(type);
    }

    @ParameterizedTest
    @EnumSource(TestEnum.class)
    void of_shouldThrowOperationException_whenUnknownOperationPassed(final TestEnum type) {
        // given & when & then
        assertThatThrownBy(() -> Operation.of(type))
                .isInstanceOf(OperationException.class)
                .hasMessage(String.format(OperationException.UNRECOGNIZABLE_OPERATION_MESSAGE, type));
    }

    private static Operation randomOperation(final Operation operation)  {
        var randomValue = randomOperation();
        while(operation.equals(randomValue)) {
            randomValue = randomOperation();
        }
        return randomValue;
    }

    private static Operation randomOperation()  {
        return List.of(Operation.values()).get(new Random().nextInt(Operation.values().length));
    }

    private enum TestEnum {
        TEST;
    }

}