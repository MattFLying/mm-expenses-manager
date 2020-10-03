package mm.expenses.manager.finance.exchangerate;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mm-expenses-manager-finance.currency")
class ExchangeRateConfig {

    private String defaultCurrency;
    private String provider;
    private String synchronizationCron;

}
