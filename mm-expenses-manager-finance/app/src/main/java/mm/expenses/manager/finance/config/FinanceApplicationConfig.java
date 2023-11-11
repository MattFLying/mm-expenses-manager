package mm.expenses.manager.finance.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.exception.config.ErrorHandlingConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Objects;

@Generated
@Configuration
@RequiredArgsConstructor
@Import({ErrorHandlingConfig.class})
class FinanceApplicationConfig {

    private static final int INITIAL_POOL_SIZE = 100;

    private final AppConfig config;

    @Bean
    ObjectMapper objectMapper() {
        final var objectMapper = new ObjectMapper();

        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
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

}
