package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@JsonPropertyOrder({"content", "currencies", "exchangeRates", "totalExchangeRates"})
public class ExchangeRatesAccumulatePage {

    @NotNull
    private final Collection<ExchangeRatePage> content;

    public Collection<ExchangeRatePage> getContent() {
        return content;
    }

    public int getCurrencies() {
        return content.size();
    }

    public int getExchangeRates() {
        return content.stream().mapToInt(page -> page.getContent().getRates().size()).sum();
    }

    public long getTotalExchangeRates() {
        return content.stream().mapToLong(ExchangeRatePage::getTotalElements).sum();
    }

    @RequiredArgsConstructor
    @JsonPropertyOrder({"content", "numberOfElements", "totalElements", "totalPages", "sortBy", "hasNext", "isFirst", "isLast"})
    public static class ExchangeRatePage {

        @NotNull
        private final ExchangeRatesDto content;

        @NotNull
        private final Page<?> ratesPage;

        public ExchangeRatesDto getContent() {
            return content;
        }

        public Integer getNumberOfElements() {
            return ratesPage.getNumberOfElements();
        }

        public Long getTotalElements() {
            return ratesPage.getTotalElements();
        }

        public Integer getTotalPages() {
            return ratesPage.getTotalPages();
        }

        public String getSortBy() {
            return ratesPage.getSort().stream().map(Sort.Order::getProperty).collect(Collectors.joining(", "));
        }

        public Boolean getHasNext() {
            return ratesPage.hasNext();
        }

        public Boolean getIsFirst() {
            return ratesPage.isFirst();
        }

        public Boolean getIsLast() {
            return ratesPage.isLast();
        }

    }

}
