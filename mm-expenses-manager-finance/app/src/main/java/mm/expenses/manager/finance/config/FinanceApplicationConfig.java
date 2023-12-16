package mm.expenses.manager.finance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.ObjectMapperConfig;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.web.config.ErrorHandlingConfig;
import mm.expenses.manager.common.web.config.OpenApiConfig;
import mm.expenses.manager.common.web.config.WebMvcConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Objects;

@Generated
@Configuration
@RequiredArgsConstructor
@Import({
        ErrorHandlingConfig.class, PaginationConfig.class, WebMvcConfig.class, OpenApiConfig.class
})
class FinanceApplicationConfig {

    private static final int INITIAL_POOL_SIZE = 100;

    private final AppConfig config;

    @Bean
    ObjectMapper objectMapper() {
        return ObjectMapperConfig.objectMapper();
    }

    @Bean
    TaskScheduler taskScheduler() {
        final var taskScheduler = new ThreadPoolTaskScheduler();

        var poolSize = config.getScheduledThreadPoolSize();
        if (Objects.isNull(poolSize)) {
            poolSize = INITIAL_POOL_SIZE;
        }
        taskScheduler.setPoolSize(poolSize);
        taskScheduler.setRemoveOnCancelPolicy(true);

        return taskScheduler;
    }

    @Bean
    PaginationHelper paginationHelper(final PaginationConfig paginationConfig) {
        return new PaginationHelper(paginationConfig);
    }

    @Bean
    OpenApiConfig openApiConfig(final AppConfig appConfig) {
        return new OpenApiConfig(appConfig);
    }

}
