package mm.expenses.manager.finance.exchangerate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.ExceptionMessage;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.pageable.PageHelper;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.exception.api.ApiNotFoundException;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesAccumulatePage;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesPage;
import mm.expenses.manager.finance.exchangerate.latest.LatestRatesCache;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("exchange-rates")
@Tag(name = "exchange-rates", description = "API of exchange rates for currencies.")
class ExchangeRateController {

    private final ExchangeRateService service;
    private final LatestRatesCache latest;
    private final ExchangeRateMapper mapper;

    @Operation(
            summary = "Finds all available exchange rates.",
            description = "Finds all available exchange rates or all according to passed options.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRatesAccumulatePage.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesAccumulatePage findAllExchangeRates(@Parameter(description = "Date of needed exchange rates.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                     @Parameter(description = "Date from of needed exchange rates.") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                     @Parameter(description = "Date to of needed exchange rates.") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                     @Parameter(description = "Page number.") @RequestParam(value = "pageNumber", required = false) @Min(0) final Integer pageNumber,
                                                     @Parameter(description = "Page size.") @RequestParam(value = "pageSize", required = false) @Min(1) final Integer pageSize) {
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException("exchange-rates-invalid-parameters", "Currencies can be filtered by date or by date from and date to at once");
        }
        if ((Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize))) {
            throw new ApiBadRequestException("exchange-rates-invalid-page-parameters", "Both page number and page size must be filled");
        }
        final var pageable = PageHelper.getPageable(pageNumber, pageSize);
        return new ExchangeRatesAccumulatePage(mapper.groupAndSortPagedResult(service.findAll(date, from, to, pageable)));
    }

    @Operation(
            summary = "Finds latest exchange rates.",
            description = "Finds all latest exchange rates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRatesPage.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesPage findLatest() {
        return new ExchangeRatesPage(mapper.groupAndSortResult(latest.getLatest().stream()));
    }

    @Operation(
            summary = "Finds all available exchange rates for specific currency code.",
            description = "Finds all available exchange rates or all according to passed options for specific currency code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRatesAccumulatePage.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesAccumulatePage findAllForCurrency(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency,
                                                   @Parameter(description = "Date from of needed exchange rates.") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                   @Parameter(description = "Date to of needed exchange rates.") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to,
                                                   @Parameter(description = "Page number.") @RequestParam(value = "pageNumber", required = false) @Min(0) final Integer pageNumber,
                                                   @Parameter(description = "Page size.") @RequestParam(value = "pageSize", required = false) @Min(1) final Integer pageSize) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if ((Objects.isNull(from) && Objects.nonNull(to)) || (Objects.nonNull(from) && Objects.isNull(to))) {
            throw new ApiBadRequestException("exchange-rates-invalid-parameters", "Currency can be filtered by date range or without any date range.");
        }
        final var pageable = PageHelper.getPageable(pageNumber, pageSize);
        return new ExchangeRatesAccumulatePage(mapper.groupAndSortPagedResult(service.findAllForCurrency(currencyCode, from, to, pageable)));
    }

    @Operation(
            summary = "Finds latest exchange rate for specific currency code.",
            description = "Finds latest exchange rate for specific currency code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRatesDto.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/{currency}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesDto findLatestForCurrency(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (currencyCode.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException("exchange-rates-invalid-currency", "Currency is not allowed");
        }
        return latest.getLatest(currencyCode)
                .map(mapper::map)
                .orElseThrow(() -> new ApiNotFoundException("exchange-rates-latest-not-found", "Latest currency for: " + currencyCode.getCode() + " not found."));
    }

    @Operation(
            summary = "Finds exchange rate for specific currency code and date.",
            description = "Finds exchange rate for specific currency code and specific date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRatesDto.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/{currency}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesDto findForCurrencyAndDate(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency,
                                            @Parameter(description = "Date of needed exchange rates.", required = true) @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (currencyCode.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException("exchange-rates-invalid-currency", "Currency is not allowed");
        }
        return service.findForCurrencyAndSpecificDate(currencyCode, date)
                .map(mapper::map)
                .orElseThrow(() -> new ApiNotFoundException("exchange-rates-not-found", "Currency for: " + currencyCode.getCode() + " and date: " + date + " not found."));
    }

}
