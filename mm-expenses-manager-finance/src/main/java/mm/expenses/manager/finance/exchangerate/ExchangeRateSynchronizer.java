package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
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
        final var provider = providers.findCurrentProviderOrAny();
        final var allCurrent = provider.getCurrentCurrencyRates();

        if (!allCurrent.isEmpty()) {
            service.createOrUpdate(allCurrent);
        } else {
            final var providerName = provider.getName();
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
                    otherProvider -> service.createOrUpdate(otherProvider.getCurrentCurrencyRates())
            );
        }
        log.info("Currencies synchronization has been done.");
    }

    /**
     * Clean rescheduled jobs scheduler when rescheduled jobs has finished completely.
     */
    @Scheduled(cron = "${app.currency.clean-reschedule-cron}")
    void cleanUpRescheduleJobsIfDone() {
        jobs.entrySet()
                .stream()
                .filter(provider -> rescheduledJobsSuccessfully.containsKey(provider.getKey()))
                .forEach(provider -> cancelCompletedRescheduledJob(provider.getKey(), provider.getValue()));
    }

    /**
     * Cancel completed rescheduled job.
     *
     * @param providerName the name of provider
     * @param job          job to be canceled
     */
    private void cancelCompletedRescheduledJob(final String providerName, final ScheduledFuture<?> job) {
        job.cancel(true);
        jobs.remove(providerName);
        rescheduledJobsSuccessfully.remove(providerName);
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
                final var allCurrent = failedProvider.getCurrentCurrencyRates();
                if (!allCurrent.isEmpty()) {
                    service.createOrUpdate(allCurrent);
                    markProviderAsDone();
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
