package mm.expenses.manager.finance.exchangerate;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@Document(collection = "exchangeRates")
@CompoundIndex(name = "currency_date_idx", def = "{'currency' : 1, 'date': 1}", unique = true)
class ExchangeRateEntity {

    @Id
    private final String id;

    private final CurrencyCode currency;

    private final Instant date;

    private final Map<String, Double> ratesByProvider;

    private final Map<String, Map<String, Object>> detailsByProvider;

    private final Instant createdAt;

}
