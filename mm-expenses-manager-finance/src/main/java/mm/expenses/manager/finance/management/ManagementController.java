package mm.expenses.manager.finance.management;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("management")
@Tag(name = "management", description = "API for management of finance.")
class ManagementController {

    private final ExchangeRateService service;

    @Operation(
            summary = "Fetch and save historical exchange rates.",
            description = "Fetching all historical exchange rates for all available currencies and saving.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "OK", content = @Content)
            }
    )
    @PostMapping(value = "/exchange-rates/history-update")
    ResponseEntity<Void> fetchAndSaveHistoricCurrencies() {
        service.historyUpdate();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
