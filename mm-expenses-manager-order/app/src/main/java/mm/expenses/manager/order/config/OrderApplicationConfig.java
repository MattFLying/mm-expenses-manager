package mm.expenses.manager.order.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Generated;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.web.WebInterceptor;
import mm.expenses.manager.common.web.config.ErrorHandlingConfig;
import mm.expenses.manager.common.web.config.OpenApiConfig;
import mm.expenses.manager.common.web.config.WebMvcConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Generated
@Configuration
@Import({
        ErrorHandlingConfig.class, PaginationConfig.class, WebMvcConfig.class, OpenApiConfig.class, WebInterceptor.class
})
class OrderApplicationConfig {

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
    PaginationHelper paginationHelper(final PaginationConfig paginationConfig) {
        return new PaginationHelper(paginationConfig);
    }

    @Bean
    OpenApiConfig openApiConfig(final AppConfig appConfig) {
        return new OpenApiConfig(appConfig);
    }

}
