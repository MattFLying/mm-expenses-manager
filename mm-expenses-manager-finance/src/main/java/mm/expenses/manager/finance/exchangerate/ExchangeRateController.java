package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("exchange-rates")
@RequiredArgsConstructor
class ExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping
    Collection<ExchangeRate> findAll() {
        return service.saveAllCurrent();
    }

    @GetMapping("/{date}")
    Collection<ExchangeRate> findAll(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
        return service.saveAllForDate(date);
    }

    @GetMapping("/{from}/{to}")
    Collection<ExchangeRate> findAll(@PathVariable("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                     @PathVariable("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return service.saveAllForDateRange(from, to);
    }


}
