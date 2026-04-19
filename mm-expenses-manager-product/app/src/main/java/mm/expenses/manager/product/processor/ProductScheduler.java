package mm.expenses.manager.product.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class ProductScheduler {

    private final ProductService service;

    @Scheduled(cron = "${app.product.clear-deleted-cron}")
    void cleanDeletedProducts() {
        service.hardDelete();
    }

    @Scheduled(cron = "${app.product.update-currencies-cron}")
    void updateCurrenciesForProductsPrices() {
        service.updateMissingCurrencies();
    }

}
