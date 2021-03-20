package mm.expenses.manager.finance.exchangerate.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class PagedExchangeRates {

    private final Page<?> page;

}
