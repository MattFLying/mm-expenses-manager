package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ApiBadRequestException;
import mm.expenses.manager.finance.exchangerate.model.CurrencyRates;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

@RestController
@RequestMapping("exchange-rates")
@RequiredArgsConstructor
class ExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping
    Collection<CurrencyRates> findAll(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                      @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                      @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException("exchange-rates-invalid-parameters", "Currencies can be filtered by date or by date from and date to at once");
        }
        return service.findAll(date, from, to);
    }

    @GetMapping(value = "/{currency}")
    Collection<CurrencyRates> findAllForCurrency(@PathVariable("currency") final String currency,
                                                 @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
                                                 @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                 @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException("exchange-rates-invalid-parameters", "Currency can be filtered by date or by date from and date to at once");
        }
        return service.findAllForCurrency(currencyCode, date, from, to);
    }

}
