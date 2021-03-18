package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.currency.provider.nbp")
class NbpApiConfig {

    private String name;
    private String url;
    private Details details;

    String getDataFormat() {
        return details.getDataFormat();
    }

    String getDefaultCurrency() {
        return details.getDefaultCurrency();
    }

    @Data
    static class Details {

        private String dataFormat;
        private String defaultCurrency;
        private int historyFromYear;
        private int maxMonthsToFetch;
        private int maxDaysToFetch;

    }

}
