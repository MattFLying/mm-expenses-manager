package mm.expenses.manager.common.utils;

import mm.expenses.manager.common.utils.i18n.CountryCode;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * Basic initialization class for testing classes.
 */
public abstract class BaseInitTest {

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
            return Stream.of(CurrencyCode.values()).map(Arguments::of);
        }
    }

    public static class CountryCodeArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(CountryCode.values()).map(Arguments::of);
        }
    }

}
