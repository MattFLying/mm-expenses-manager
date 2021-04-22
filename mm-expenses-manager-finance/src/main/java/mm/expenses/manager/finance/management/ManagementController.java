package mm.expenses.manager.finance.management;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.common.pageable.PageHelper;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesTrailsPage;
import mm.expenses.manager.finance.exchangerate.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailService;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("management")
@Tag(name = "management", description = "API for management of finance.")
class ManagementController {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateTrailService exchangeRateTrailService;

    @Operation(
            summary = "Fetch and save historical exchange rates.",
            description = "Fetching all historical exchange rates for all available currencies and saving.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "OK", content = @Content)
            }
    )
    @PostMapping(value = "/exchange-rates/history-update")
    ResponseEntity<Void> fetchAndSaveHistoricCurrencies() {
        exchangeRateService.historyUpdate();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Finds exchange rates trails.",
            description = "Finds operations logs of operations on exchange rates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExchangeRatesTrailsPage.class)
                            )),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(value = "/exchange-rates/trails", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRatesTrailsPage findAllTrails(@Parameter(description = "Date of needed exchange rates trail.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                          @Parameter(description = "Type of exchange rate operation.") @RequestParam(value = "operation", required = false) final TrailOperation operation,
                                          @Parameter(description = "State of exchange rate operation.") @RequestParam(value = "state", required = false) final State state,
                                          @Parameter(description = "Page number.") @RequestParam(value = "pageNumber", required = false) @Min(0) final Integer pageNumber,
                                          @Parameter(description = "Page size.") @RequestParam(value = "pageSize", required = false) @Min(1) final Integer pageSize) {
        if ((Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED);
        }
        final var pageable = PageHelper.getPageable(pageNumber, pageSize);
        return new ExchangeRatesTrailsPage(exchangeRateTrailService.findTrails(date, operation, state, pageable));
    }

}
