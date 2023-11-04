package mm.expenses.manager.finance.currency;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.api.currency.CurrenciesApi;
import mm.expenses.manager.finance.api.currency.model.CurrencyDto;
import mm.expenses.manager.finance.api.currency.model.CurrencyInfoDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("currencies")
class CurrenciesController implements CurrenciesApi {

    private final CurrenciesService currenciesService;

    private final CurrencyMapper mapper;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CurrencyDto findAllAvailableCurrencyCodes() {
        return mapper.mapToCurrencyDto(currenciesService.getAllAvailableCurrencyCodes().stream().toList());
    }

    @Override
    @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public CurrencyDto findCurrentCurrencyCode() {
        return mapper.mapToCurrencyDto(List.of(currenciesService.getCurrentCurrency().getCode()));
    }

    @Override
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public CurrencyInfoDto findAllAvailableCurrenciesInformation() {
        return mapper.mapToCurrencyInfo(mapper.mapToCurrencyCodeDto(currenciesService.getAllAvailableCurrencies()));
    }

}
