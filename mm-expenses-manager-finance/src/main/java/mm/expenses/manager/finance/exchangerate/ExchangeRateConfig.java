package mm.expenses.manager.finance.exchangerate;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.currency")
class ExchangeRateConfig {

    private String defaultCurrency;
    private String defaultProvider;
    private String synchronizationCron;

}
