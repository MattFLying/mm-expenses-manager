package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.currency")
public class CurrencyRatesConfig {

    private String defaultProvider;
    private CurrencyCode defaultCurrency;
    private String synchronizationCron;
    private String rescheduleWhenSynchronizationFailedCron;
    private String cleanRescheduleCron;

    public Set<CurrencyCode> getAllRequiredCurrenciesCode() {
        return Stream.of(CurrencyCode.values()).filter(code -> !code.equals(CurrencyCode.UNDEFINED) || !code.equals(defaultCurrency)).collect(Collectors.toSet());
    }

}
