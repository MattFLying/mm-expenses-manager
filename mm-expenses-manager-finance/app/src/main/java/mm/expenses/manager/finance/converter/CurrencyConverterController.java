package mm.expenses.manager.finance.converter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.CalculationsApi;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("calculations")
class CurrencyConverterController implements CalculationsApi {

    private final CurrencyConverterService currencyConverterService;
    private final CurrencyConverterMapper mapper;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CurrencyConversionResponse convertRate(@RequestParam(value = "from") String from,
                                                  @RequestParam(value = "to") String to,
                                                  @RequestParam(value = "value") BigDecimal value,
                                                  @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                  @RequestParam(value = "id", required = false) String id) {
        final var currencyCodeFrom = CurrencyCode.getCurrencyFromString(from, false);
        final var currencyCodeTo = CurrencyCode.getCurrencyFromString(to, false);
        if (Objects.isNull(value) || BigDecimalWrapper.of(value).compareTo(BigDecimal.ZERO) < 1) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO);
        }
        final var result = Objects.nonNull(date)
                ? currencyConverterService.convertForDate(currencyCodeFrom, currencyCodeTo, value, date, id)
                : currencyConverterService.convertLatest(currencyCodeFrom, currencyCodeTo, value, id);
        return mapper.map(result);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<CurrencyConversionResponse> convertMultipleRates(@Valid @RequestBody List<@Valid CurrencyConversionRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_MULTIPLE_CONVERSION_NULL_REQUEST);
        }

        val errors = new ArrayList<CurrencyConversionRequest>();
        requests.forEach(dto -> {
            var isFromNull = false;
            if (Objects.isNull(dto.getFrom())) {
                errors.add(dto);
                isFromNull = true;
            }
            if (Objects.isNull(dto.getTo())) {
                errors.add(dto);
            }

            if (!isFromNull) {
                if (Objects.isNull(dto.getFrom().getValue()) || dto.getFrom().getValue() == 0 || Objects.isNull(dto.getFrom().getCode())) {
                    errors.add(dto);
                }
            }
        });

        if (!errors.isEmpty()) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.withParameters(errors));
        }
        return mapper.map(currencyConverterService.convertMultiple(requests));
    }

}
