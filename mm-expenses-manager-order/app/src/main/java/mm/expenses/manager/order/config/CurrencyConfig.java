package mm.expenses.manager.order.config;

import lombok.Data;
import lombok.Generated;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Generated
@Configuration
@ConfigurationProperties(prefix = "app.currency")
public class CurrencyConfig {

    private CurrencyCode defaultCurrency;

}
