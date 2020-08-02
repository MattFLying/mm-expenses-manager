package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import org.springframework.web.bind.annotation.*;

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


}
