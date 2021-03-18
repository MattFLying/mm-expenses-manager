package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ApiFeignClientException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component("${app.currency.provider.nbp.name}")
@RequiredArgsConstructor
class NbpCurrencyProvider implements CurrencyRateProvider<NbpCurrencyRate> {

    private final NbpClient client;
    private final NbpApiConfig nbpApiConfig;

    @Override
    public String getName() {
        return nbpApiConfig.getName();
    }

    @Override
    public CurrencyCode getDefaultCurrency() {
        return CurrencyCode.getCurrencyFromString(nbpApiConfig.getDefaultCurrency());
    }

    @Override
    public Collection<NbpCurrencyRate> getAllHistoricalCurrencies() {
        return new NbpHistoryUpdater(nbpApiConfig, this).fetchHistoricalCurrencies();
    }

    @Override
    public Optional<NbpCurrencyRate> getCurrentCurrencyRate(final CurrencyCode currency) {
        final var table = TableType.findTableForCurrency(currency);
        try {
            return client.fetchCurrentExchangeRateForCurrencyFromTableType(table.name(), currency.getCode(), nbpApiConfig.getDataFormat())
                    .flatMap(dto -> dto.getRates()
                            .stream()
                            .findFirst()
                            .map(rateDto -> NbpCurrencyRate.of(currency, table, rateDto))
                    );
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for currency: {}", table, exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NbpCurrencyRate> getCurrencyRateForDate(final CurrencyCode currency, final LocalDate date) {
        final var table = TableType.findTableForCurrency(currency);
        try {
            return client.fetchExchangeRateForCurrencyFromTableTypeAndDate(table.name(), currency.getCode(), date, nbpApiConfig.getDataFormat())
                    .flatMap(dto -> dto.getRates()
                            .stream()
                            .findFirst()
                            .map(rateDto -> NbpCurrencyRate.of(currency, table, rateDto))
                    );
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for currency: {} and date : {}", table, date, exception);
            return Optional.empty();
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRateForDateRange(final CurrencyCode currency, final LocalDate from, final LocalDate to) {
        final var table = TableType.findTableForCurrency(currency);
        try {
            return client.fetchExchangeRateForCurrencyFromTableTypeAndDateRange(table.name(), currency.getCode(), from, to, nbpApiConfig.getDataFormat())
                    .<Collection<NbpCurrencyRate>>map(dto -> dto.getRates()
                            .stream()
                            .map(rateDto -> NbpCurrencyRate.of(currency, table, rateDto))
                            .collect(Collectors.toList())
                    ).orElse(Collections.emptyList());
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for currency: {} and date range {} - {}", table, from, to, exception);
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrentCurrencyRates() {
        try {
            return client.fetchCurrentAllExchangeRatesForTableType(getAvailableTableType(), nbpApiConfig.getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for table: {}", getAvailableTableType(), exception);
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRatesForDate(final LocalDate date) {
        try {
            return client.fetchAllExchangeRatesForTableTypeAndDate(getAvailableTableType(), date, nbpApiConfig.getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for table: {} and date: {}", getAvailableTableType(), date, exception);
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRatesForDateRange(final LocalDate from, final LocalDate to) {
        try {
            return client.fetchAllExchangeRatesForTableTypeAndDateRange(getAvailableTableType(), from, to, nbpApiConfig.getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for table: {} and date range {} - {}", getAvailableTableType(), from, to, exception);
            return Collections.emptyList();
        }
    }

    private Collection<NbpCurrencyRate> map(final NbpClient.TableExchangeRatesDto tableExchangeRatesDto) {
        final var date = tableExchangeRatesDto.getEffectiveDate();
        final var table = TableType.parse(tableExchangeRatesDto.getTable());
        final var tableNumber = tableExchangeRatesDto.getNo();
        final var currenciesAsStrings = Arrays.stream(CurrencyCode.values()).map(CurrencyCode::getCode).collect(Collectors.toSet());

        return tableExchangeRatesDto.getRates().stream()
                .filter(filterAvailableCurrenciesOnly(currenciesAsStrings))
                .map(rateDto -> new NbpCurrencyRate(CurrencyCode.getCurrencyFromString(rateDto.getCode()), date, rateDto.getMid(), table, tableNumber))
                .filter(currency -> !currency.getCurrency().equals(CurrencyCode.UNDEFINED))
                .collect(Collectors.toList());
    }

    private Predicate<? super NbpClient.TableRateDto> filterAvailableCurrenciesOnly(Set<String> currenciesAsStrings) {
        return dto -> currenciesAsStrings.stream().anyMatch(currency -> currency.equalsIgnoreCase(dto.getCode()));
    }

    private String getAvailableTableType() {
        return TableType.A.name();
    }

}
