package mm.expenses.manager.finance.financial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.common.CurrencyProviderType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyDetailsParser {

    private final ObjectMapper mapper;

    public JSONObject parseCurrencyRateDetailsToJson(final CurrencyRate currencyRate) {
        final var json = new JSONObject();
        try {
            final var detailsOpt = currencyRate.getDetails();
            if (detailsOpt.isPresent()) {
                final var details = detailsOpt.get();
                final var detailsAsString = mapper.writeValueAsString(details);

                json.put(details.getType().getProviderName(), new JSONParser().parse(detailsAsString));
            } else {
                log.debug("Passed currency rate: {} does not has any details", currencyRate);
            }
        } catch (final JsonProcessingException | ParseException exception) {
            log.error("Error occurred during parsing details of currency rate: {}", currencyRate, exception);
        }
        return json;
    }

    public Map<CurrencyProviderType, CurrencyRateProvider.CurrencyDetails> parseJsonDetailsToCurrencyRateDetailsTypes(final JSONObject currencyDetailsAsJson) {
        final var detailsMap = new EnumMap<CurrencyProviderType, CurrencyRateProvider.CurrencyDetails>(CurrencyProviderType.class);
        try {
            if (!currencyDetailsAsJson.isEmpty()) {
                for (final var type : CurrencyProviderType.values()) {
                    final var detailsForType = currencyDetailsAsJson.get(type.getProviderName());
                    detailsMap.put(
                            type,
                            mapper.readValue(mapper.writeValueAsString(detailsForType), type.getClassType())
                    );
                }
            } else {
                log.debug("Passed details for currency rate does not has any details");
            }
        } catch (final JsonProcessingException exception) {
            log.error("Error occurred during parsing currency rate details: {}", currencyDetailsAsJson, exception);
        }
        return detailsMap;
    }

}
