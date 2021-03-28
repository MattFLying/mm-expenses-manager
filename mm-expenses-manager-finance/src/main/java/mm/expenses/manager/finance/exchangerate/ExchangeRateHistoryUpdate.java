package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (!providers.isAnyProviderAvailable()) {
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
