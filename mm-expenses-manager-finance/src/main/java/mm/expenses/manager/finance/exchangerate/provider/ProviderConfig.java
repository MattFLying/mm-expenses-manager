package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;

@Data
public abstract class ProviderConfig {

    private String name;
    private String url;
    private Details details;

    public String getDataFormat() {
        return details.getDataFormat();
    }

    public String getDefaultCurrency() {
        return details.getDefaultCurrency();
    }

    @Data
    public static class Details {

        private String dataFormat;
        private String defaultCurrency;
        private int historyFromYear;
        private int maxMonthsToFetch;
        private int maxDaysToFetch;

    }

}
