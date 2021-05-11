package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.exception.HistoricalCurrencyException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateHistoryUpdate {

    private final CurrencyProviders providers;
    private final ExchangeRateCommand command;

    private final Lock lock = new ReentrantLock();

    /**
     * Update historical exchange rates.
     */
    synchronized void update() {
        if (!lock()) {
            return;
        }

        try {
            executeHistoryUpdate();
        } finally {
            releaseLock();
        }
    }

    /**
     * Update historical exchange rates process.
     */
    private void executeHistoryUpdate() {
        log.info("Currencies history update in progress.");
        try {
            final var provider = providers.getProvider();
            command.saveHistory(provider.getHistoricCurrencies().fetch());
        } catch (final HistoricalCurrencyException unknownException) {
            log.warn("Error occurred during currencies history update process.", unknownException);
            providers.executeOnAllProviders(provider -> {
                final var providerName = providers.getProviderName(provider);
                log.info("Currencies history update retrying for another provider: {} in progress.", providerName);
                try {
                    command.saveHistory(provider.getHistoricCurrencies().fetch());
                } catch (final HistoricalCurrencyException unknownExceptionForOtherProvider) {
                    log.warn("Something went wrong during update process for provider: {}", providerName);
                }
                log.info("Currencies history update retried for another provider: {} has been done.", providerName);
            });
        }
        log.info("Currencies history update has been done.");
    }

    /**
     * Try acquire lock.
     */
    private synchronized boolean lock() {
        return lock.tryLock();
    }

    /**
     * Release lock.
     */
    private synchronized void releaseLock() {
        lock.unlock();
    }

}
