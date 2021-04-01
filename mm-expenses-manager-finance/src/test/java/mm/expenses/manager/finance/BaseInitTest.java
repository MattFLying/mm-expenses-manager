package mm.expenses.manager.finance;

import mm.expenses.manager.common.i18n.CurrencyCode;
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

    public static class CurrencyCodeArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(CurrencyCode.values()).filter(code -> !code.equals(CurrencyCode.PLN) && !code.equals(CurrencyCode.UNDEFINED)).map(Arguments::of);
        }
    }

}
