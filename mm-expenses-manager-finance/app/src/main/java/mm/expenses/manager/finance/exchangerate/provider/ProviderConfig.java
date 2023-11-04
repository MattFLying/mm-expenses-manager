package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import lombok.Generated;

@Data
@Generated
public abstract class ProviderConfig {

    private String name;
    private String url;
    private Details details;

    public String getDataFormat() {
        return details.getDataFormat();
    }

    @Data
    public static class Details {

        private String dataFormat;
        private int historyFromYear;
        private int maxDaysToFetch;

    }

}
