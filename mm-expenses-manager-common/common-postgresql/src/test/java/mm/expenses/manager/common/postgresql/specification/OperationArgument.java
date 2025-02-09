package mm.expenses.manager.common.postgresql.specification;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class OperationArgument implements ArgumentsProvider {

    @Override
    public Stream<Arguments> provideArguments(final ExtensionContext context) {
        return Stream.of(Operation.values()).map(Arguments::of);
    }

    public static class OperationExceptEqualArgument implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Operation.values()).filter(operation -> !operation.equals(Operation.equal)).map(Arguments::of);
        }

    }

}
