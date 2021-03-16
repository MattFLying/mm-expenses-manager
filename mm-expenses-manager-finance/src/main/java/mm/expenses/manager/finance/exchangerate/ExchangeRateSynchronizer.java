package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateSynchronizer {

    private final ExchangeRateService service;

    @Scheduled(cron = "${app.currency.synchronization-cron}")
    void dailySynchronization() {
        log.info("Currencies synchronization in progress.");
        service.saveAllCurrent();
        log.info("Currencies synchronization has been done.");
    }

}
