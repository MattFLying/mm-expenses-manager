package mm.expenses.manager.finance.currency;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.currency.dto.CurrencyDto;
import mm.expenses.manager.finance.currency.dto.CurrencyInfoDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("currencies")
@Tag(name = "Currencies", description = "Provide API for currencies.")
class CurrenciesController {

    private final CurrenciesService currenciesService;

    @Operation(
            summary = "Finds all available currency codes.",
            description = "Check currently all available currency codes that can be used in different endpoints.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CurrencyDto.class)
                            ))
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    CurrencyDto findAllAvailableCurrencyCodes() {
        return new CurrencyDto(currenciesService.getAllAvailableCurrencyCodes());
    }

    @Operation(
            summary = "Finds currently used currency code.",
            description = "Check currently used currency code in system..",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CurrencyDto.class)
                            ))
            }
    )
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    CurrencyDto findCurrentCurrencyCode() {
        return new CurrencyDto(List.of(currenciesService.getCurrentCurrency().getCode()));
    }

    @Operation(
            summary = "Finds information for all available currency codes.",
            description = "Check information for currently all available currency codes that can be used in different endpoints.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CurrencyInfoDto.class)
                            ))
            }
    )
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    CurrencyInfoDto findAllAvailableCurrenciesInformation() {
        return new CurrencyInfoDto(currenciesService.getAllAvailableCurrencies());
    }

}
