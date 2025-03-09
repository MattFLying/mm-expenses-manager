package mm.expenses.manager.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.stream.Stream;

import static mm.expenses.manager.product.product.ProductHelper.DEFAULT_CURRENCY;

@RunWith(JUnitParamsRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ProductApplication.class)
public class ProductApplicationSpringTest extends BaseInitTest {

    @Autowired
    protected ObjectMapper objectMapper;

    public static class CurrencyCodeArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(CurrencyCode.values())
                    .filter(currency -> !DEFAULT_CURRENCY.equals(currency))
                    .filter(currency -> !CurrencyCode.UNDEFINED.equals(currency))
                    .map(Arguments::of);
        }
    }

}
