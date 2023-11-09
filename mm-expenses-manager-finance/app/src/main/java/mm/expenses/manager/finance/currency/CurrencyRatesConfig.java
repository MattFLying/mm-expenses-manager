package mm.expenses.manager.finance.currency;

import lombok.Data;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.currency")
public class CurrencyRatesConfig {

    private String defaultProvider;
    private CurrencyCode defaultCurrency;
    private String synchronizationCron;
    private String rescheduleWhenSynchronizationFailedCron;
    private String cleanRescheduleCron;

}
