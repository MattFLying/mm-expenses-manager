package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.Data;
import lombok.Generated;
import mm.expenses.manager.finance.exchangerate.provider.ProviderConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Generated
@Configuration("nbp-provider-config")
@ConfigurationProperties(prefix = "app.currency.provider.nbp")
class NbpApiConfig extends ProviderConfig {

}
