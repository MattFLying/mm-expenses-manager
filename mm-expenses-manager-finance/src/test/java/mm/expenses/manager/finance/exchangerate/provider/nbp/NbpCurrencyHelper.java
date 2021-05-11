package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;

import java.time.LocalDate;
import java.util.List;

public class NbpCurrencyHelper {

    public static final TableType TABLE_TYPE = TableType.A;
    public static final String PROVIDER_NAME = "nbp";

    public static final int MAX_DAYS_TO_FETCH = 30;
    public static final int HISTORY_FROM_YEAR = 2021;

    public static NbpClient.TableRateDto createTableRateDto(final CurrencyCode currencyCode, final Double rate) {
        final var tableRateDto = new NbpClient.TableRateDto();
        tableRateDto.setCode(currencyCode.getCode());
        tableRateDto.setMid(rate);
        return tableRateDto;
    }

    public static NbpClient.TableExchangeRatesDto createTableExchangeRatesDto(final TableType tableType, final String tableNumber, final LocalDate date, final NbpClient.TableRateDto... tableRateDto) {
        final var tableExchangeRatesDto = new NbpClient.TableExchangeRatesDto();
        tableExchangeRatesDto.setNo(tableNumber);
        tableExchangeRatesDto.setTable(tableType.name());
        tableExchangeRatesDto.setEffectiveDate(date);
        tableExchangeRatesDto.setRates(List.of(tableRateDto));
        return tableExchangeRatesDto;
    }

    public static NbpCurrencyRate createNbpCurrencyRate(final CurrencyCode currency, final LocalDate date, final Double rate, final TableType tableType, final String tableNumber) {
        return new NbpCurrencyRate(currency, date, rate, tableType, tableNumber);
    }

    public static CurrencyRate createCurrencyRate(final CurrencyCode currency, final LocalDate date, final Double rate, final TableType tableType, final String tableNumber) {
        return new NbpCurrencyRate(currency, date, rate, tableType, tableNumber);
    }

}
