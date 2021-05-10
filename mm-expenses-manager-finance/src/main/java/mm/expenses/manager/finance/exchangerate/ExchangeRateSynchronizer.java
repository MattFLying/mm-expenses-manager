package mm.expenses.manager.finance.exchangerate;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

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
        final var provider = providers.getProvider();
        try {
            final var allCurrent = provider.getCurrentCurrencyRates();
            if (allCurrent.isEmpty()) {
                log.debug("For provider: {} there are no current currency rates. Rescheduling and call another provider in progress...", provider.getName());
                rescheduleProviderAndCallAnother(provider);
            } else {
                service.synchronize(allCurrent);
            }
        } catch (final CurrencyProviderException exception) {
            log.warn("Cannot fetch current currency rates for provider: {}. Rescheduling and call another provider in progress...", provider.getName(), exception);
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
            log.error("Cannot clean up rescheduled jobs.", exception);
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
            log.error("Cannot cancel rescheduled job for provider: {}", providerName, exception);
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
                    new CronTrigger(providers.getGlobalConfig().getRescheduleWhenSynchronizationFailedCron())
            );
            if (Objects.nonNull(scheduleFailedJob)) {
                jobs.put(providerName, scheduleFailedJob);
            }
        }

        providers.executeOnAllProviders(
                otherProvider -> !otherProvider.getName().equalsIgnoreCase(providers.getProviderName()),
                otherProvider -> {
                    try {
                        service.synchronize(otherProvider.getCurrentCurrencyRates());
                    } catch (final CurrencyProviderException exception) {
                        log.warn("Cannot fetch currency rates for provider: {}", otherProvider.getName(), exception);
                        if (exception.isHttpError()) {
                            exception.getClientStatus()
                                    .ifPresent(status -> log.warn("Server error for provider: {} with error: {}", otherProvider.getName(), exception.getClientMessage().orElse(exception.getMessage()), exception));
                        } else {
                            log.warn("Unknown error for provider: {} with error: {}", otherProvider.getName(), exception.getMessage(), exception);
                        }
                        rescheduleProviderAndCallAnother(otherProvider);
                    }
                }
        );
    }

    /**
     * Reschedule fetching latest exchange rates if failed at the first time.
     */
    @Generated
    @RequiredArgsConstructor
    static class RescheduleFailedProvider implements Runnable {

        private final CurrencyRateProvider<?> failedProvider;
        private final ExchangeRateService service;
        private final ConcurrentHashMap<String, Boolean> rescheduledJobsSuccessfully;

        @Override
        public void run() {
            final var failedProviderName = failedProvider.getName();
            log.info("Retrying fetch current currencies for provider: {}", failedProviderName);
            if (service.findToday().isEmpty()) {
                try {
                    final var allCurrent = failedProvider.getCurrentCurrencyRates();
                    if (!allCurrent.isEmpty()) {
                        service.synchronize(allCurrent);
                        markProviderAsDone();
                    }
                } catch (final CurrencyProviderException exception) {
                    log.warn("Cannot fetch currency rates for failed provider: {}", failedProviderName, exception);
                }
            } else {
                markProviderAsDone();
            }
        }

        /**
         * If reschedule job has finished completely then mark this jbo as finished.
         */
        private void markProviderAsDone() {
            final var failedProviderName = failedProvider.getName();
            if (!rescheduledJobsSuccessfully.containsKey(failedProviderName)) {
                rescheduledJobsSuccessfully.put(failedProviderName, true);
                log.info("Retrying fetch current currencies for provider: {} has completed.", failedProviderName);
            }
        }

    }

}
