package mm.expenses.manager.finance.exchangerate.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static mm.expenses.manager.finance.exchangerate.exception.ExceptionConstant.EXTERNAL_CURRENCY_PROVIDER_ERROR;
import static mm.expenses.manager.finance.exchangerate.exception.ExceptionConstant.EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum FinanceExceptionMessage implements ExceptionType {
    // currency
    CURRENCY_NOT_ALLOWED("currency-not-available-error", "Currency is not available."),

    // currency external providers
    CURRENCY_PROVIDER_NOT_FOUND("currency-provider-not-found-error", "Unable to find exchange rate provider."),

    CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES(EXTERNAL_CURRENCY_PROVIDER_ERROR, "Unable to fetch current currency rates. Client provider error."),
    CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES(EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR, "Something went wrong during fetch current currency rates."),

    CURRENCY_PROVIDER_FEIGN_SINGLE_CURRENCY_AND_DATE(EXTERNAL_CURRENCY_PROVIDER_ERROR, "Unable to fetch currency rate for currency: %s and date: %s. Client provider error."),
    CURRENCY_PROVIDER_UNKNOWN_SINGLE_CURRENCY_AND_DATE(EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR, "Something went wrong during fetch currency rate for currency: %s and date: %s."),

    CURRENCY_PROVIDER_FEIGN_SINGLE_CURRENCY_AND_DATE_RANGE(EXTERNAL_CURRENCY_PROVIDER_ERROR, "Unable to fetch currency rate for currency: %s and date between: %s - %s. Client provider error."),
    CURRENCY_PROVIDER_UNKNOWN_SINGLE_CURRENCY_AND_DATE_RANGE(EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR, "Something went wrong during fetch currency rate for currency: %s and date between: %s - %s."),

    CURRENCY_PROVIDER_FEIGN_SINGLE_CURRENCY(EXTERNAL_CURRENCY_PROVIDER_ERROR, "Unable to fetch current currency rate for currency: %s. Client provider error."),
    CURRENCY_PROVIDER_UNKNOWN_SINGLE_CURRENCY(EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR, "Something went wrong during fetch current currency rate for currency: %s."),

    CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES_AND_DATE(EXTERNAL_CURRENCY_PROVIDER_ERROR, "Unable to fetch currency rates for date: %s. Client provider error."),
    CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES_AND_DATE(EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR, "Something went wrong during fetch currency rates for date: %s."),

    CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES_AND_DATE_RANGE(EXTERNAL_CURRENCY_PROVIDER_ERROR, "Unable to fetch currency rates for date range between: %s - %s. Client provider error."),
    CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES_AND_DATE_RANGE(EXTERNAL_CURRENCY_PROVIDER_INTERNAL_ERROR, "Something went wrong during fetch currency rates for date range between: %s - %s."),

    NBP_CURRENCY_PROVIDER_UNKNOWN_TABLE("unknown-currency-assignment-table-for-provider-error", "There are more table types than currently allowed."),

    // exchange rate
    SAVE_HISTORIC_EXCHANGE_RATES("update-historic-exchange-rates-error", "Unable to save historical currency rates."),
    SAVE_HISTORIC_EXCHANGE_RATES_UNKNOWN_ERROR("update-historic-exchange-rates-unknown-error", "Something went wrong during fetching historical currencies."),

    EXCHANGE_RATES_INVALID_DATE_TO("exchange-rate-invalid-date-to-error", "Invalid data, could not find date to."),
    EXCHANGE_RATES_INVALID_DATE_FROM("exchange-rate-invalid-date-from-error", "Invalid data, could not find date from."),

    SAVE_OR_UPDATE_EXCHANGE_RATES("save-exchange-rate-error", "Unable to save or update currency rates."),

    // rest
    PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED("page-size-and-page-number-must-be-passed-error", "Both page number and page size must be filled"),

    CURRENCY_FILTER_BY_DATE_OR_DATE_RANGE_ONLY("currency-filtering-by-date-or-date-range-error", "Currencies can be filtered by date or by date from and date to at once"),
    CURRENCY_FILTER_BY_DATE_RANGE("currency-filtering-by-date-range-error", "Currency can be filtered by date range or without any date range."),
    CURRENCY_FOR_CODE_AND_DATE_NOT_FOUND("currency-for-code-and-date-not-found-error", "Currency for: %s and date: %s not found."),
    LATEST_CURRENCY_FOR_CODE_NOT_FOUND("latest-currency-for-code-not-found-error", "Latest currency for: %s not found.");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return String.format(message, parameters);
    }

    @Override
    public ExceptionType withParameters(final Object... params) {
        if (Objects.nonNull(parameters) && ArrayUtils.isNotEmpty(parameters)) {
            final var tempList = new ArrayList<>(Arrays.asList(parameters));
            tempList.addAll(new ArrayList<>(Arrays.asList(params)));
            parameters = tempList.toArray();
        } else {
            parameters = params;
        }
        return this;
    }

}
