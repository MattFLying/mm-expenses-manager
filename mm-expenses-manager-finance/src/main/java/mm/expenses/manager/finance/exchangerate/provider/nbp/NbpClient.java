package mm.expenses.manager.finance.exchangerate.provider.nbp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import mm.expenses.manager.exception.api.feign.ApiFeignClientException;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.exception.FinanceExceptionMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Generated
@FeignClient(name = "${app.currency.provider.nbp.client}", url = "${app.currency.provider.nbp.url}")
interface NbpClient {

    @GetMapping(value = "tables/{tableType}/")
    Collection<TableExchangeRatesDto> fetchCurrentAllExchangeRatesForTableType(@PathVariable("tableType") final String tableType,
                                                                               @RequestParam(value = "format") final String format) throws ApiFeignClientException;

    @GetMapping(value = "tables/{tableType}/{date}/")
    Collection<TableExchangeRatesDto> fetchAllExchangeRatesForTableTypeAndDate(@PathVariable("tableType") final String tableType,
                                                                               @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                                               @RequestParam(value = "format") final String format) throws ApiFeignClientException;

    @GetMapping(value = "tables/{tableType}/{from}/{to}/")
    Collection<TableExchangeRatesDto> fetchAllExchangeRatesForTableTypeAndDateRange(@PathVariable("tableType") final String tableType,
                                                                                    @PathVariable("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                                                    @PathVariable("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                                                    @RequestParam(value = "format") String format) throws ApiFeignClientException;

    @GetMapping(value = "rates/{tableType}/{currencyCode}/")
    Optional<ExchangeRateDto> fetchCurrentExchangeRateForCurrencyFromTableType(@PathVariable("tableType") final String tableType,
                                                                               @PathVariable("currencyCode") final String currencyCode,
                                                                               @RequestParam(value = "format") final String format) throws ApiFeignClientException;

    @GetMapping(value = "rates/{tableType}/{currencyCode}/{date}/")
    Optional<ExchangeRateDto> fetchExchangeRateForCurrencyFromTableTypeAndDate(@PathVariable("tableType") final String tableType,
                                                                               @PathVariable("currencyCode") final String currencyCode,
                                                                               @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                                               @RequestParam(value = "format") final String format) throws ApiFeignClientException;

    @GetMapping(value = "rates/{tableType}/{currencyCode}/{from}/{to}/")
    Optional<ExchangeRateDto> fetchExchangeRateForCurrencyFromTableTypeAndDateRange(@PathVariable("tableType") final String tableType,
                                                                                    @PathVariable("currencyCode") final String currencyCode,
                                                                                    @PathVariable("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                                                    @PathVariable("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                                                    @RequestParam(value = "format") final String format) throws ApiFeignClientException;

    @Data
    @Generated
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class TableRateDto {
        private String code;
        private Double mid;
    }

    @Data
    @Generated
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class TableExchangeRatesDto {
        private String table;
        private String no;
        private LocalDate effectiveDate;
        private Collection<TableRateDto> rates;
    }

    @Data
    @Generated
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class RateDto {
        private String no;
        private LocalDate effectiveDate;
        private Double mid;
    }

    @Data
    @Generated
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class ExchangeRateDto {
        private String table;
        private String code;
        private Collection<RateDto> rates;
    }

    default String getAvailableTableType() throws CurrencyProviderException {
        final var unsupportedTypes = Stream.of(TableType.values()).filter(type -> !type.equals(TableType.A) && !type.equals(TableType.UNKNOWN)).collect(Collectors.toSet());
        if (!unsupportedTypes.isEmpty()) {
            throw new CurrencyProviderException(FinanceExceptionMessage.NBP_CURRENCY_PROVIDER_UNKNOWN_TABLE);
        }
        return TableType.A.name();
    }

}
