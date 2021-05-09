package mm.expenses.manager.finance.converter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.finance.converter.dto.CurrencyConversionDto;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("calculations")
@Tag(name = "Calculations", description = "Provide API to currencies conversion.")
class CurrencyConverterController {

    private final CurrencyConverterService currencyConverterService;
    private final CurrencyConverterMapper mapper;

    @Operation(
            summary = "Convert single currency with latest exchange rate or with given date.",
            description = "Allow to calculate exchange rate for specific currency code with additional parameters.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CurrencyConversionDto.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    CurrencyConversionDto convertRate(@Parameter(description = "Currency code from which will be made conversion.") @RequestParam(value = "from") final String from,
                                      @Parameter(description = "Currency code to which will be made conversion.") @RequestParam(value = "to") final String to,
                                      @Parameter(description = "Value of currency from which will be made conversion.") @RequestParam(value = "value") final BigDecimal value,
                                      @Parameter(description = "Specific date of exchange rate.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                      @Parameter(description = "Id to be used for calculated result and retrieved with it. Just for recognition purposes, id won't be processed.") @RequestParam(value = "id", required = false) final String id) {
        final var currencyCodeFrom = CurrencyCode.getCurrencyFromString(from, false);
        final var currencyCodeTo = CurrencyCode.getCurrencyFromString(to, false);
        if (currencyCodeFrom.equals(CurrencyCode.UNDEFINED) || currencyCodeTo.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_NOT_ALLOWED);
        }
        if (value.compareTo(BigDecimal.ZERO) < 1) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO);
        }
        final var result = Objects.nonNull(date)
                ? currencyConverterService.convertForDate(currencyCodeFrom, currencyCodeTo, value, date, id)
                : currencyConverterService.convertLatest(currencyCodeFrom, currencyCodeTo, value, id);
        return mapper.map(result);
    }

}
