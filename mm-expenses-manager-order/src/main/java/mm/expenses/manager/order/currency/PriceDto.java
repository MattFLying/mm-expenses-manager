package mm.expenses.manager.order.currency;

import lombok.Builder;
import lombok.Data;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class PriceDto {

    private final Currency currency;
    private final BigDecimal amount;

    @ConstructorProperties({"currency", "amount"})
    public PriceDto(final Currency currency, final BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

}
