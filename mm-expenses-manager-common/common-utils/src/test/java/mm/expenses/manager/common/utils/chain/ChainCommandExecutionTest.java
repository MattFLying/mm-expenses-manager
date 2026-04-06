package mm.expenses.manager.common.utils.chain;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChainCommandExecutionTest {

    private static final String CONTEXT_ARGUMENT_KEY = "test";
    private static final String CONTEXT_ARGUMENT_VALUE = "test";

    @Test
    void hasNext_shouldReturnFalse_whenNoNextStepDefined() {
        // given
        final var chain = new FirstChain();

        // when
        final var hasNext = chain.hasNext();

        // then
        assertFalse(hasNext);
    }

    @Test
    void hasNext_shouldReturnFalse_whenNextStepIsNull() {
        // given
        final var chain = new FirstChain();
        chain.setNextHandler(null);

        // when
        final var hasNext = chain.hasNext();

        // then
        assertFalse(hasNext);
    }

    @Test
    void hasNext_shouldReturnTrue_whenNextStepDefined() {
        // given
        final var chain = new FirstChain();
        chain.setNextHandler(new SecondChain());

        // when
        final var hasNext = chain.hasNext();

        // then
        assertTrue(hasNext);
    }

    @Test
    void setNextHandler_shouldAssignContextFromStepOneToStepTwo_whenNextHandlerIsDefined() {
        // given
        final var chain = new FirstChain();
        chain.getContext().addArgument(CONTEXT_ARGUMENT_KEY, CONTEXT_ARGUMENT_VALUE);

        // when
        chain.setNextHandler(new SecondChain());

        // then
        assertThat(chain.next.getContext()).isEqualTo(chain.getContext());

        final var argumentOpt = chain.next.getContext().getArgument(CONTEXT_ARGUMENT_KEY);
        assertThat(argumentOpt).isPresent();

        final var argument = argumentOpt.get();
        assertThat(argument).isEqualTo(CONTEXT_ARGUMENT_VALUE);
    }

    @Test
    void setNextHandler_shouldNotAssignContextFromStepOneToStepTwo_whenNextHandlerIsNull() {
        // given
        final var chain = new FirstChain();
        chain.getContext().addArgument(CONTEXT_ARGUMENT_KEY, CONTEXT_ARGUMENT_VALUE);

        // when
        chain.setNextHandler(null);

        // then
        assertThat(chain.next).isNull();
    }

    @Test
    void handleRequest_shouldCorrectlyHandleRequest() {
        // given
        final var requestValue = 5;
        final var chain = new FirstChain();
        chain.getContext().addArgument(CONTEXT_ARGUMENT_KEY, CONTEXT_ARGUMENT_VALUE);
        chain.setNextHandler(new SecondChain());

        // when
        final var result = chain.handleRequest(requestValue);

        // then
        assertThat(result).isNotNull().isInstanceOf(String.class);
        assertThat(result).isEqualTo(String.valueOf(requestValue));
    }

    @Test
    void build_shouldCorrectlyBuildChain() {
        // given
        final var requestValue = 5;
        final var chain = ChainCommandExecution.build(
                new FirstChain(),
                new SecondChain()
        );

        // when
        final var result = chain.handleRequest(requestValue);

        // then
        assertThat(result).isNotNull().isInstanceOf(String.class);
        assertThat(result).isEqualTo(String.valueOf(requestValue));
    }

    public static class FirstChain extends ChainCommandExecution<Integer, String> {

        @Override
        public String handleRequest(final Integer value) {
            if (!hasNext()) {
                setNextHandler(new SecondChain());
            }
            return next.handleRequest(value);
        }

    }

    public static class SecondChain extends ChainCommandExecution<Integer, String> {

        @Override
        public void setNextHandler(final ChainCommandExecution<Integer, String> nextStep) {
            super.setNextHandler(null);
        }

        @Override
        public String handleRequest(final Integer value) {
            return Objects.nonNull(value)
                    ? String.valueOf(value)
                    : "";
        }

    }

}