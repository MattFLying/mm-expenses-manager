package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import lombok.Generated;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Generated
@Configuration
@ConfigurationProperties(prefix = "app.currency")
public class CurrencyRatesConfig {

    private String defaultProvider;
    private CurrencyCode defaultCurrency;
    private String synchronizationCron;
    private String rescheduleWhenSynchronizationFailedCron;
    private String cleanRescheduleCron;

}
