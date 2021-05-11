package mm.expenses.manager.finance.exchangerate;

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
import mm.expenses.manager.exception.api.ApiNotFoundException;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheMapper;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestRatesCacheService;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesAccumulatePage;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesPage;
import mm.expenses.manager.finance.pageable.PageFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("exchange-rates")
@Tag(name = "Exchange Rates", description = "Provide API to check exchange rates for different currencies.")
class ExchangeRateController {

    private final ExchangeRateService service;
    private final LatestRatesCacheService latest;
    private final ExchangeRateMapper mapper;
    private final ExchangeRateCacheMapper cacheMapper;
    private final PageFactory pageFactory;

    @Operation(
            summary = "Finds exchange rates for all available currency codes.",
            description = "Check exchange rate for all available currency codes. Can be parametrized to specify result.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExchangeRatesAccumulatePage.class)
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
    ExchangeRatesAccumulatePage findAllExchangeRates(@Parameter(description = "Specific date to be found.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                     @Parameter(description = "Specific start date to be found.") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                     @Parameter(description = "Specific end date to be found.") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                     @Parameter(description = "Page number.") @RequestParam(value = "pageNumber", required = false) @Min(0) final Integer pageNumber,
                                                     @Parameter(description = "Page size.") @RequestParam(value = "pageSize", required = false) @Min(1) final Integer pageSize) {
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_FILTER_BY_DATE_OR_DATE_RANGE_ONLY);
        }
        if ((Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED);
        }
        final var pageable = pageFactory.getPageable(pageNumber, pageSize);
        return new ExchangeRatesAccumulatePage(mapper.groupAndSortPagedResult(service.findAll(date, from, to, pageable)));
    }

    @Operation(
            summary = "Finds latest exchange rate for all available currency codes.",
            description = "Check latest exchange rate for all available currency codes. Always retrieve the latest available exchange rates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExchangeRatesPage.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesPage findLatest() {
        return new ExchangeRatesPage(cacheMapper.groupAndSortResultCache(latest.getLatest().stream()));
    }

    @Operation(
            summary = "Finds exchange rates for specific currency code.",
            description = "Check exchange rate for specific currency code. Can be parametrized to specify result.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExchangeRatesAccumulatePage.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesAccumulatePage findAllForCurrency(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency,
                                                   @Parameter(description = "Specific start date to be found.") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                   @Parameter(description = "Specific end date to be found.") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                   @Parameter(description = "Page number.") @RequestParam(value = "pageNumber", required = false) @Min(0) final Integer pageNumber,
                                                   @Parameter(description = "Page size.") @RequestParam(value = "pageSize", required = false) @Min(1) final Integer pageSize) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (currencyCode.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_NOT_ALLOWED);
        }
        if ((Objects.isNull(from) && Objects.nonNull(to)) || (Objects.nonNull(from) && Objects.isNull(to))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_FILTER_BY_DATE_RANGE);
        }
        final var pageable = pageFactory.getPageable(pageNumber, pageSize);
        return new ExchangeRatesAccumulatePage(mapper.groupAndSortPagedResult(service.findAllForCurrency(currencyCode, from, to, pageable)));
    }

    @Operation(
            summary = "Finds latest exchange rate for specific currency code.",
            description = "Check latest exchange rate for specific currency code. Always retrieve the latest available exchange rate.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExchangeRatesDto.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/{currency}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesDto findLatestForCurrency(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (currencyCode.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_NOT_ALLOWED);
        }
        return latest.getLatest(currencyCode)
                .map(cacheMapper::mapCache)
                .orElseThrow(() -> new ApiNotFoundException(FinanceExceptionMessage.LATEST_CURRENCY_FOR_CODE_NOT_FOUND.withParameters(currencyCode.getCode())));
    }

    @Operation(
            summary = "Finds exchange rate for specific currency code and date.",
            description = "Check exchange rate for specific currency code and date. Always retrieve exchange rate for given date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExchangeRatesDto.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/{currency}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesDto findForCurrencyAndDate(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency,
                                            @Parameter(description = "Specific date to be found.", required = true) @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (currencyCode.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_NOT_ALLOWED);
        }
        return service.findForCurrencyAndSpecificDate(currencyCode, date)
                .map(mapper::map)
                .orElseThrow(() -> new ApiNotFoundException(FinanceExceptionMessage.CURRENCY_FOR_CODE_AND_DATE_NOT_FOUND.withParameters(currencyCode.getCode(), date)));
    }

}
