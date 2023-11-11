package mm.expenses.manager.finance.converter;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.beans.exception.api.ApiBadRequestException;
import mm.expenses.manager.finance.api.calculations.CalculationsApi;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionDto;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("calculations")
class CurrencyConverterController implements CalculationsApi {

    private final CurrencyConverterService currencyConverterService;
    private final CurrencyConverterMapper mapper;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CurrencyConversionDto convertRate(@RequestParam(value = "from") String from,
                                             @RequestParam(value = "to") String to,
                                             @RequestParam(value = "value") Double value,
                                             @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @RequestParam(value = "id", required = false) String id) {
        final var currencyCodeFrom = CurrencyCode.getCurrencyFromString(from, false);
        final var currencyCodeTo = CurrencyCode.getCurrencyFromString(to, false);
        if (BigDecimal.valueOf(value).compareTo(BigDecimal.ZERO) < 1) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO);
        }
        final var result = Objects.nonNull(date)
                ? currencyConverterService.convertForDate(currencyCodeFrom, currencyCodeTo, BigDecimal.valueOf(value), date, id)
                : currencyConverterService.convertLatest(currencyCodeFrom, currencyCodeTo, BigDecimal.valueOf(value), id);
        return mapper.map(result);
    }

}
