package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

@Data
public abstract class ProviderConfig {

    private String name;
    private String url;
    private Details details;

    public String getDataFormat() {
        return details.getDataFormat();
    }

    public CurrencyCode getCurrency() {
        return details.getCurrency();
    }

    @Data
    public static class Details {

        private String dataFormat;
        private CurrencyCode currency;
        private int historyFromYear;
        private int maxMonthsToFetch;
        private int maxDaysToFetch;

    }

}
