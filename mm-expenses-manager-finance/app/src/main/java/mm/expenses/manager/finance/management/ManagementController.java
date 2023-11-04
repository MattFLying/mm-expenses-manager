package mm.expenses.manager.finance.management;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.finance.api.management.ManagementApi;
import mm.expenses.manager.finance.api.management.model.ExchangeRatesTrailsPage;
import mm.expenses.manager.finance.api.management.model.OperationType;
import mm.expenses.manager.finance.api.management.model.StateType;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailService;
import mm.expenses.manager.finance.pageable.PageFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("management")
class ManagementController implements ManagementApi {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateTrailService exchangeRateTrailService;
    private final TrailMapper mapper;
    private final PageFactory pageFactory;

    @Override
    @PostMapping(value = "/exchange-rates/history-update")
    public void fetchAndSaveHistoricCurrencies() {
        exchangeRateService.historyUpdate();
        ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @GetMapping(value = "/exchange-rates/trails", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExchangeRatesTrailsPage findAllTrails(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                 @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                 @RequestParam(value = "operation", required = false) OperationType operation,
                                                 @RequestParam(value = "state", required = false) StateType state) {
        if ((Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED);
        }
        final var pageable = pageFactory.getPageable(pageNumber, pageSize);

        return mapper.map(exchangeRateTrailService.findTrails(date, mapper.map(operation), mapper.map(state), pageable));
    }

}
