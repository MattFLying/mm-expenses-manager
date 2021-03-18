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
import mm.expenses.manager.exception.ApiBadRequestException;
import mm.expenses.manager.finance.exchangerate.model.dto.ExchangeRatesDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("exchange-rates")
@Tag(name = "exchange-rates", description = "API of exchange rates for currencies.")
class ExchangeRateController {

    private final ExchangeRateService service;
    private final ExchangeRateMapper mapper;

    @Operation(
            summary = "Finds all available exchange rates.",
            description = "Finds all available exchange rates or all according to passed options.",
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
                    )
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    Collection<ExchangeRatesDto> findAllExchangeRates(@Parameter(description = "Date of needed exchange rates.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                      @Parameter(description = "Date from of needed exchange rates.") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                      @Parameter(description = "Date to of needed exchange rates.") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException("exchange-rates-invalid-parameters", "Currencies can be filtered by date or by date from and date to at once");
        }
        return service.findAll(date, from, to).stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Finds all available exchange rates for specific currency code.",
            description = "Finds all available exchange rates or all according to passed options for specific currency code.",
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
                    )
            }
    )
    @GetMapping(value = "/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
    Collection<ExchangeRatesDto> findAllForCurrency(@Parameter(description = "Currency code for expected exchange rates.", required = true) @PathVariable("currency") final String currency,
                                                    @Parameter(description = "Date of needed exchange rates.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                    @Parameter(description = "Date from of needed exchange rates.") @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                    @Parameter(description = "Date to of needed exchange rates.") @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException("exchange-rates-invalid-parameters", "Currency can be filtered by date or by date from and date to at once");
        }
        return service.findAllForCurrency(currencyCode, date, from, to).stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Fetch and save historical currencies.",
            description = "Fetching all historical exchange rates for all available currencies and saving.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "OK", content = @Content)
            }
    )
    @PostMapping(value = "/history-update")
    ResponseEntity<Void> fetchAndSaveHistoricCurrencies() {
        service.historyUpdate();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
