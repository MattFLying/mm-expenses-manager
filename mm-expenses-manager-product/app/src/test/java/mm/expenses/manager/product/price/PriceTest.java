package mm.expenses.manager.product.price;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.BaseInitTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static mm.expenses.manager.product.price.Price.ROUND_PRICE_VALUE_DIGITS;
import static mm.expenses.manager.product.price.Price.ROUND_PRICE_VALUE_MODE;
import static org.assertj.core.api.Assertions.assertThat;

class PriceTest extends BaseInitTest {

    @Test
    void shouldReturnZeroValueWhenValueIsNull() {
        // given
        final var price = new Price(null, null);

        // when
        final var result = price.getValue();

        // then
        assertThat(result).isEqualTo(BigDecimal.ZERO.setScale(ROUND_PRICE_VALUE_DIGITS, ROUND_PRICE_VALUE_MODE));
    }

    @Test
    void shouldReturnUndefinedCurrencyWhenCurrencyIsNull() {
        // given
        final var price = new Price(null, null);

        // when
        final var result = price.getCurrency();

        // then
        assertThat(result).isEqualTo(CurrencyCode.UNDEFINED);
    }

}