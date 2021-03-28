package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.currency")
public class CurrencyRatesConfig {

    private String defaultProvider;
    private String defaultCurrency;
    private String synchronizationCron;
    private String rescheduleWhenSynchronizationFailedCron;
    private String cleanRescheduleCron;

}
