package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateService {

    private final CurrencyProviders providers;
    private final ExchangeRateCommand command;
    private final ExchangeRateQuery query;
    private final ExchangeRateConfig config;

    void historyUpdate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (providers.isAnyProviderAvailable()) {
                throw providers.apiInternalErrorExceptionForNoProvider();
            }
            try {
                final var provider = providers.findCurrentProviderOrAny();
                log.info("Currencies history update in progress.");
                command.saveHistory(provider.getAllHistoricalCurrencies());
                log.info("Currencies history update has been done.");
            } catch (final Exception unknownException) {
                log.warn("Error occurred during currencies history update process.", unknownException);
                providers.executeOnAllProviders(provider -> {
                    log.info("Currencies history update retrying for another provider: {} in progress.", provider.getName());
                    command.saveHistory(provider.getAllHistoricalCurrencies());
                    log.info("Currencies history update retried for another provider: {}  has been done.", provider.getName());
                });
            }
        });
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createOrUpdate(final Collection<T> exchangeRates) {
        return command.createOrUpdate(exchangeRates);
    }

    Stream<Page<ExchangeRate>> findAll(final LocalDate date, final LocalDate from, final LocalDate to, final Pageable pageable) {
        return query.findAllCurrenciesRates(getAllRequiredCurrenciesCode(), date, from, to, pageable);
    }

    Stream<Page<ExchangeRate>> findAllForCurrency(final CurrencyCode currency, final LocalDate from, final LocalDate to, final Pageable pageable) {
        return query.findAllForCurrencyRates(currency, from, to, pageable);
    }

    Page<ExchangeRate> findLatest() {
        final var page = PageRequest.of(0, getAllRequiredCurrenciesCode().size(), Sort.by("date").descending());
        return query.findAllLatest(page);
    }

    Optional<ExchangeRate> findLatestForCurrency(final CurrencyCode currency) {
        return query.findLatestForCurrency(currency);
    }

    Optional<ExchangeRate> findForCurrencyAndSpecificDate(final CurrencyCode currency, final LocalDate date) {
        return query.findByCurrencyAndDate(currency, date);
    }

    private Set<CurrencyCode> getAllRequiredCurrenciesCode() {
        return Stream.of(CurrencyCode.values()).filter(code -> !code.equals(CurrencyCode.UNDEFINED) || !code.equals(CurrencyCode.valueOf(config.getDefaultCurrency()))).collect(Collectors.toSet());
    }

}
