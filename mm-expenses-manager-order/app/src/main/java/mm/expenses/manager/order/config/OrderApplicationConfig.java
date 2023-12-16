package mm.expenses.manager.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import mm.expenses.manager.common.beans.ObjectMapperConfig;
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
        return ObjectMapperConfig.objectMapper();
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
