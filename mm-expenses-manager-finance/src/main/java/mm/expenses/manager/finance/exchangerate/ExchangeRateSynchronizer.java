package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateSynchronizer {

    private final CurrencyProviders providers;
    private final ExchangeRateService service;
    private final TaskScheduler scheduler;

    private final ConcurrentHashMap<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> rescheduledJobsSuccessfully = new ConcurrentHashMap<>();

    /**
     * Scheduler for update latest exchange rates.
     * Creates reschedule job if currently used provider was not able to fetch latest data.
     */
    @Scheduled(cron = "${app.currency.synchronization-cron}")
    void scheduleUpdateLatestExchangeRates() {
        log.info("Currencies synchronization in progress.");
        final var provider = providers.findDefaultProviderOrAny();
        try {
            final var allCurrent = provider.getCurrentCurrencyRates();
            if (allCurrent.isEmpty()) {
                rescheduleProviderAndCallAnother(provider);
            } else {
                service.createOrUpdate(allCurrent);
            }
        } catch (final CurrencyProviderException exception) {
            log.warn("Cannot fetch current currency rates for provider: {}", provider.getName(), exception);
            rescheduleProviderAndCallAnother(provider);
        }
        log.info("Currencies synchronization has been done.");
    }

    /**
     * Clean rescheduled jobs scheduler when rescheduled jobs has finished completely.
     */
    @Scheduled(cron = "${app.currency.clean-reschedule-cron}")
    void cleanUpRescheduleJobsIfDone() {
        try {
            jobs.entrySet()
                    .stream()
                    .filter(provider -> rescheduledJobsSuccessfully.containsKey(provider.getKey()))
                    .forEach(provider -> cancelCompletedRescheduledJob(provider.getKey(), provider.getValue()));
        } catch (final Exception exception) {
            log.warn("Cannot clean up rescheduled jobs", exception);
        }
    }

    /**
     * Cancel completed rescheduled job.
     *
     * @param providerName the name of provider
     * @param job          job to be canceled
     */
    private void cancelCompletedRescheduledJob(final String providerName, final ScheduledFuture<?> job) {
        try {
            job.cancel(true);
            jobs.remove(providerName);
            rescheduledJobsSuccessfully.remove(providerName);
        } catch (final Exception exception) {
            log.warn("Cannot cancel rescheduled job for provider: {}", providerName, exception);
        }
    }

    /**
     * Reschedule update latest exchange rates for given provider.
     *
     * @param provider currencies provider
     */
    private void rescheduleProviderAndCallAnother(final CurrencyRateProvider<?> provider) {
        final var providerName = provider.getName();
        log.info("Reschedule fetching latest exchange rates for provider: {}", providerName);
        if (!jobs.containsKey(providerName) && !rescheduledJobsSuccessfully.containsKey(providerName)) {
            final var scheduleFailedJob = scheduler.schedule(
                    new RescheduleFailedProvider(provider, service, rescheduledJobsSuccessfully),
                    new CronTrigger(providers.getConfig().getRescheduleWhenSynchronizationFailedCron())
            );
            if (Objects.nonNull(scheduleFailedJob)) {
                jobs.put(providerName, scheduleFailedJob);
            }
        }

        providers.executeOnAllProviders(
                otherProvider -> !otherProvider.getName().equalsIgnoreCase(providers.getDefaultProvider()),
                otherProvider -> {
                    try {
                        service.createOrUpdate(otherProvider.getCurrentCurrencyRates());
                    } catch (final CurrencyProviderException exception) {
                        log.warn("Cannot fetch currency rates for provider: {}", otherProvider.getName(), exception);
                        if (exception.isHttpError()) {
                            exception.getClientStatus()
                                    .filter(HttpStatus::is5xxServerError)
                                    .ifPresent(status -> {
                                        log.warn("Server error for provider: {} with error: {}", otherProvider.getName(), exception.getClientMessage().orElse(exception.getMessage()));
                                        rescheduleProviderAndCallAnother(otherProvider);
                                    });
                        }
                    }
                }
        );
    }

    /**
     * Reschedule fetching latest exchange rates if failed at the first time.
     */
    @RequiredArgsConstructor
    private static class RescheduleFailedProvider implements Runnable {

        private final CurrencyRateProvider<?> failedProvider;
        private final ExchangeRateService service;
        private final ConcurrentHashMap<String, Boolean> rescheduledJobsSuccessfully;

        @Override
        public void run() {
            log.info("Retrying fetch current currencies for provider: {}", failedProvider.getName());
            if (service.findLatest().isEmpty()) {
                try {
                    final var allCurrent = failedProvider.getCurrentCurrencyRates();
                    if (!allCurrent.isEmpty()) {
                        service.createOrUpdate(allCurrent);
                        markProviderAsDone();
                    }
                } catch (final CurrencyProviderException exception) {
                    log.warn("Cannot fetch currency rates for failed provider: {}", failedProvider.getName(), exception);
                }
            } else {
                markProviderAsDone();
            }
        }

        /**
         * If reschedule job has finished completely then mark this jbo as finished.
         */
        private void markProviderAsDone() {
            if (!rescheduledJobsSuccessfully.containsKey(failedProvider.getName())) {
                rescheduledJobsSuccessfully.put(failedProvider.getName(), true);
                log.info("Retrying fetch current currencies for provider: {} has completed.", failedProvider.getName());
            }
        }

    }

}
