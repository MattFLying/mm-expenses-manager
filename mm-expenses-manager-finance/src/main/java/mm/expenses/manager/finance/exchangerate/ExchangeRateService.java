package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.financial.CurrencyRate;
import mm.expenses.manager.finance.financial.CurrencyRateProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateService {

    private final @Qualifier("${mm-expenses-manager-finance.currency.provider}") CurrencyRateProvider<? extends CurrencyRate> nbpService;
    private final ExchangeRateCreator creator;

    @Value("${mm-expenses-manager-finance.currency.default}")
    private String defaultCurrency;

    Optional<ExchangeRate> saveCurrent(final CurrencyCode currency) {
        final var current = nbpService.getCurrentCurrencyRate(currency);
        if (current.isPresent()) {
            return creator.create(current.get());
        }
        return Optional.empty();
    }

    Optional<ExchangeRate> saveForDate(final CurrencyCode currency, final LocalDate date) {
        final var forDate = nbpService.getCurrencyRateForDate(currency, date);
        if (forDate.isPresent()) {
            return creator.create(forDate.get());
        }
        return Optional.empty();
    }

    Collection<ExchangeRate> saveForDateRange(final CurrencyCode currency, final LocalDate from, final LocalDate to) {
        final var forDateRange = nbpService.getCurrencyRateForDateRange(currency, from, to);
        if (!forDateRange.isEmpty()) {
            return creator.createForDateRange(currency, forDateRange);
        }
        return Collections.emptyList();
    }

    Collection<ExchangeRate> saveAllCurrent() {
        final var allCurrent = nbpService.getCurrentCurrencyRates();
        if (!allCurrent.isEmpty()) {
            return creator.createAll(allCurrent);
        }
        return Collections.emptyList();
    }

    Collection<ExchangeRate> saveAllForDate(final LocalDate date) {
        final var allForDate = nbpService.getCurrencyRatesForDate(date);
        if (!allForDate.isEmpty()) {
            return creator.createAll(allForDate);
        }
        return Collections.emptyList();
    }

    Collection<ExchangeRate> saveAllForDateRange(final LocalDate from, final LocalDate to) {
        final var allForDateRange = nbpService.getCurrencyRatesForDateRange(from, to);
        if (!allForDateRange.isEmpty()) {
            return creator.createAllForDateRange(allForDateRange);
        }
        return Collections.emptyList();
    }

}
