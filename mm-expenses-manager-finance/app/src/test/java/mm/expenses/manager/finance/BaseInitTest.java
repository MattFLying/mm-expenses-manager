package mm.expenses.manager.finance;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic initialization class for testing classes.
 */
public abstract class BaseInitTest {

    public static final CurrencyCode DEFAULT_CURRENCY = CurrencyCode.PLN;

    /**
     * Prepare setup steps for each test before execution or nothing will happen when it won't be overriden
     */
    protected void setupBeforeEachTest() {

    }

    /**
     * Prepare setup steps for each test after execution or nothing will happen when it won't be overriden
     */
    protected void setupAfterEachTest() {

    }

    @BeforeEach
    protected void beforeEachTest() {
        setupBeforeEachTest();
    }

    @AfterEach
    protected void afterEachTest() {
        setupAfterEachTest();
    }

    public static class CurrencyCodeArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(CurrencyCode.values()).filter(code -> !code.equals(DEFAULT_CURRENCY) && !code.equals(CurrencyCode.UNDEFINED)).map(Arguments::of);
        }
    }

    public static class CurrencyCodeCombinationsArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            final var allCurrencies = Stream.of(CurrencyCode.values()).filter(code -> !code.equals(DEFAULT_CURRENCY) && !code.equals(CurrencyCode.UNDEFINED)).collect(Collectors.toSet());
            return allCurrencies.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            currencyCode -> allCurrencies.stream().filter(code -> !code.equals(currencyCode)).collect(Collectors.toSet())
                    ))
                    .entrySet()
                    .stream()
                    .map(x -> x.getValue().stream().map(y -> Pair.of(x.getKey(), y)).collect(Collectors.toSet()))
                    .flatMap(Collection::stream)
                    .map(Arguments::of);
        }
    }

    public static class TrailOperationArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            var success = Stream.of(TrailOperation.values()).map(operation -> operation.withStatus(TrailOperation.State.SUCCESS)).map(Arguments::of);
            var failed = Stream.of(TrailOperation.values()).map(operation -> operation.withStatus(TrailOperation.State.ERROR)).map(Arguments::of);
            return Stream.concat(success, failed);
        }
    }

}
