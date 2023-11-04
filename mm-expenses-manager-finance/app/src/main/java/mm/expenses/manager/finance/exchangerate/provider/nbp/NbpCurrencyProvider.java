package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.feign.ApiFeignClientException;
import mm.expenses.manager.finance.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;
import mm.expenses.manager.finance.exchangerate.provider.ProviderConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

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
    public Collection<NbpCurrencyRate> getCurrentCurrencyRates() throws CurrencyProviderException {
        try {
            return client.fetchCurrentAllExchangeRatesForTableType(client.getAvailableTableType(), getDataFormat())
                    .stream()
                    .map(this::map)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (final ApiFeignClientException exception) {
            throw new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES, exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES, exception);
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
            throw new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES_AND_DATE_RANGE.withParameters(from, to), exception);
        } catch (final Exception exception) {
            throw new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES_AND_DATE_RANGE.withParameters(from, to), exception);
        }
    }

    private Collection<NbpCurrencyRate> map(final NbpClient.TableExchangeRatesDto tableExchangeRatesDto) {
        final var date = tableExchangeRatesDto.getEffectiveDate();
        final var table = TableType.parse(tableExchangeRatesDto.getTable());
        final var tableNumber = tableExchangeRatesDto.getNo();
        final var currenciesAsStrings = Arrays.stream(CurrencyCode.values()).map(CurrencyCode::getCode).collect(Collectors.toSet());

        return tableExchangeRatesDto.getRates().stream()
                .filter(dto -> currenciesAsStrings.stream().anyMatch(currency -> currency.equalsIgnoreCase(dto.getCode())))
                .map(rateDto -> new NbpCurrencyRate(CurrencyCode.getCurrencyFromString(rateDto.getCode()), date, rateDto.getMid(), table, tableNumber))
                .filter(currency -> !currency.getCurrency().equals(CurrencyCode.UNDEFINED))
                .collect(Collectors.toList());
    }

    private String getDataFormat() {
        return config.getDataFormat();
    }

}
