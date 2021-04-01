package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.ApiFeignClientException;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;
import mm.expenses.manager.finance.exchangerate.provider.ProviderConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component("${app.currency.provider.nbp.name}")
@RequiredArgsConstructor
class NbpCurrencyProvider implements CurrencyRateProvider<NbpCurrencyRate> {

    private final NbpClient client;
    private final NbpApiConfig config;

    @Override
    public ProviderConfig getProviderConfig() {
        return config;
    }

    @Override
    public HistoricCurrencies<NbpCurrencyRate> getHistoricCurrencies() {
        return new NbpHistoryUpdater(this);
    }

    @Override
    public Optional<NbpCurrencyRate> getCurrentCurrencyRate(final CurrencyCode currency) throws CurrencyProviderException {
        try {
            final var table = TableType.findTableForCurrency(currency);
            return client.fetchCurrentExchangeRateForCurrencyFromTableType(table.name(), currency.getCode(), getDataFormat())
                    .flatMap(dto -> dto.getRates()
                            .stream()
                            .findFirst()
                            .map(rateDto -> NbpCurrencyRate.of(currency, table, rateDto))
                    );
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException(format("Cannot fetch current currency rate for currency: %s. Client provider error.", currency), exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(format("Something went wrong during fetch current currency rate for currency: %s.", currency), exception);
        }
    }

    @Override
    public Optional<NbpCurrencyRate> getCurrencyRateForDate(final CurrencyCode currency, final LocalDate date) throws CurrencyProviderException {
        try {
            final var table = TableType.findTableForCurrency(currency);
            return client.fetchExchangeRateForCurrencyFromTableTypeAndDate(table.name(), currency.getCode(), date, getDataFormat())
                    .flatMap(dto -> dto.getRates()
                            .stream()
                            .findFirst()
                            .map(rateDto -> NbpCurrencyRate.of(currency, table, rateDto))
                    );
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException(format("Cannot fetch currency rate for currency: %s and date: %s. Client provider error.", currency, date), exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(format("Something went wrong during fetch currency rate for currency: %s and date: %s.", currency, date), exception);
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRateForDateRange(final CurrencyCode currency, final LocalDate from, final LocalDate to) throws CurrencyProviderException {
        try {
            final var table = TableType.findTableForCurrency(currency);
            return client.fetchExchangeRateForCurrencyFromTableTypeAndDateRange(table.name(), currency.getCode(), from, to, getDataFormat())
                    .<Collection<NbpCurrencyRate>>map(dto -> dto.getRates()
                            .stream()
                            .map(rateDto -> NbpCurrencyRate.of(currency, table, rateDto))
                            .collect(Collectors.toList())
                    ).orElse(Collections.emptyList());
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException(format("Cannot fetch currency rate for currency: %s and date between: %s - %s. Client provider error.", currency, from, to), exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(format("Something went wrong during fetch currency rate for currency: %s and date between: %s - %s.", currency, from, to), exception);
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrentCurrencyRates() throws CurrencyProviderException {
        try {
            return client.fetchCurrentAllExchangeRatesForTableType(client.getAvailableTableType(), getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException("Cannot fetch current currency rates. Client provider error.", exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException("Something went wrong during fetch current currency rates.", exception);
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRatesForDate(final LocalDate date) throws CurrencyProviderException {
        try {
            return client.fetchAllExchangeRatesForTableTypeAndDate(client.getAvailableTableType(), date, getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException(format("Cannot fetch currency rates for date: %s. Client provider error.", date), exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(format("Something went wrong during fetch currency rates for date: %s.", date), exception);
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRatesForDateRange(final LocalDate from, final LocalDate to) throws CurrencyProviderException {
        try {
            return client.fetchAllExchangeRatesForTableTypeAndDateRange(client.getAvailableTableType(), from, to, getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException(format("Cannot fetch currency rates for date range between: %s - %s. Client provider error.", from, to), exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(format("Something went wrong during fetch currency rates for date range between: %s - %s.", from, to), exception);
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

    private String getDataFormat() {
        return config.getDataFormat();
    }

    private Predicate<? super NbpClient.TableRateDto> filterAvailableCurrenciesOnly(Set<String> currenciesAsStrings) {
        return dto -> currenciesAsStrings.stream().anyMatch(currency -> currency.equalsIgnoreCase(dto.getCode()));
    }

}
