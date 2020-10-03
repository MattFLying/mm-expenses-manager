package mm.expenses.manager.finance.nbp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ApiFeignClientException;
import mm.expenses.manager.finance.financial.CurrencyRateProvider;
import mm.expenses.manager.finance.nbp.NbpClient.TableExchangeRatesDto;
import mm.expenses.manager.finance.nbp.model.NbpCurrencyRate;
import mm.expenses.manager.finance.nbp.model.NbpCurrencyRate.NbpDetails;
import mm.expenses.manager.finance.nbp.model.TableType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component("${mm-expenses-manager-finance.currency.provider}")
@RequiredArgsConstructor
class NbpCurrencyProvider implements CurrencyRateProvider<NbpCurrencyRate> {

    private final NbpClient client;
    private final NbpApiConfig nbpApiConfig;

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
            return client.fetchCurrentExchangeRateForCurrencyFromTableType(table.name(), currency.getCode(), nbpApiConfig.getDataFormat()).flatMap(dto -> map(dto, currency, table));
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for currency: {}", table, exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NbpCurrencyRate> getCurrencyRateForDate(final CurrencyCode currency, final LocalDate date) {
        final var table = TableType.findTableForCurrency(currency);
        try {
            return client.fetchExchangeRateForCurrencyFromTableTypeAndDate(table.name(), currency.getCode(), date, nbpApiConfig.getDataFormat()).flatMap(dto -> map(dto, currency, table));
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
                    .<Collection<NbpCurrencyRate>>map(
                            dto -> dto.getRates()
                                    .stream()
                                    .map(rateDto -> NbpCurrencyRate.builder()
                                            .currency(currency)
                                            .date(rateDto.getEffectiveDate())
                                            .nbpDetails(buildNbpDetails(table, rateDto.getNo(), rateDto.getMid()))
                                            .build())
                                    .collect(Collectors.toList())
                    ).orElse(Collections.emptyList());
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for currency: {} and date range {} - {}", table, from, to, exception);
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrentCurrencyRates() {
        return Stream.concat(getCurrentExchangeRatesForTable(TableType.A), getCurrentExchangeRatesForTable(TableType.B))
                .map(this::map)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRatesForDate(final LocalDate date) {
        return Stream.concat(getExchangeRatesForDateAndTable(TableType.A, date), getExchangeRatesForDateAndTable(TableType.B, date))
                .map(this::map)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<NbpCurrencyRate> getCurrencyRatesForDateRange(final LocalDate from, final LocalDate to) {
        return Stream.concat(getExchangeRatesForDateRangeAndTable(TableType.A, from, to), getExchangeRatesForDateRangeAndTable(TableType.B, from, to))
                .map(this::map)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private NbpDetails buildNbpDetails(final TableType table, final String tableNumber, final Double rate) {
        return NbpDetails.builder().tableType(table).tableNumber(tableNumber).rate(rate).build();
    }

    private NbpDetails map(final TableType tableType, final String tableNumber, final Double rate) {
        return buildNbpDetails(tableType, tableNumber, rate);
    }

    private NbpCurrencyRate map(final CurrencyCode currency, final LocalDate date, final Double rate, final TableType tableType, final String tableNumber) {
        return NbpCurrencyRate.builder()
                .currency(currency)
                .date(date)
                .nbpDetails(map(tableType, tableNumber, rate))
                .build();
    }

    private Optional<NbpCurrencyRate> map(final NbpClient.ExchangeRateDto dto, final CurrencyCode currency, final TableType table) {
        return dto.getRates().stream()
                .findFirst()
                .map(rateDto -> map(currency, rateDto.getEffectiveDate(), rateDto.getMid(), table, rateDto.getNo()));
    }

    private Collection<NbpCurrencyRate> map(final TableExchangeRatesDto tableExchangeRatesDto) {
        final var date = tableExchangeRatesDto.getEffectiveDate();
        final var table = TableType.parse(tableExchangeRatesDto.getTable());
        final var tableNumber = tableExchangeRatesDto.getNo();

        return tableExchangeRatesDto.getRates().stream()
                .map(rateDto -> map(CurrencyCode.getCurrencyFromString(rateDto.getCode()), date, rateDto.getMid(), table, tableNumber))
                .filter(currency -> !currency.getCurrency().equals(CurrencyCode.UNDEFINED))
                .collect(Collectors.toList());
    }

    private Stream<TableExchangeRatesDto> getCurrentExchangeRatesForTable(final TableType tableType) {
        try {
            return client.fetchCurrentAllExchangeRatesForTableType(tableType.name(), nbpApiConfig.getDataFormat()).stream();
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for table: {}", tableType, exception);
            return Stream.empty();
        }
    }

    private Stream<TableExchangeRatesDto> getExchangeRatesForDateAndTable(final TableType tableType, final LocalDate date) {
        try {
            return client.fetchAllExchangeRatesForTableTypeAndDate(tableType.name(), date, nbpApiConfig.getDataFormat()).stream();
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for table: {} and date: {}", tableType, date, exception);
            return Stream.empty();
        }
    }

    private Stream<TableExchangeRatesDto> getExchangeRatesForDateRangeAndTable(final TableType tableType, final LocalDate from, final LocalDate to) {
        try {
            return client.fetchAllExchangeRatesForTableTypeAndDateRange(tableType.name(), from, to, nbpApiConfig.getDataFormat()).stream();
        } catch (final ApiFeignClientException exception) {
            log.error("Could not fetch data for table: {} and date range {} - {}", tableType, from, to, exception);
            return Stream.empty();
        }
    }

}
