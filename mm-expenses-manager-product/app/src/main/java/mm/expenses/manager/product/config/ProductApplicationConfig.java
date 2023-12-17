package mm.expenses.manager.product.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import mm.expenses.manager.common.beans.ObjectMapperConfig;
import mm.expenses.manager.common.web.config.ApplicationConfig;
import mm.expenses.manager.common.web.config.ErrorHandlingConfig;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.web.config.OpenApiConfig;
import mm.expenses.manager.common.web.config.WebMvcConfig;
import mm.expenses.manager.common.beans.converter.BigDecimalToDecimal128Converter;
import mm.expenses.manager.common.beans.converter.Decimal128ToBigDecimalConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Generated
@Configuration
@Import({
        ErrorHandlingConfig.class, PaginationConfig.class, WebMvcConfig.class, OpenApiConfig.class, ApplicationConfig.class
})
class ProductApplicationConfig {

    @Bean
    ObjectMapper objectMapper() {
        return ObjectMapperConfig.objectMapper();
    }

    @Bean
    MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new BigDecimalToDecimal128Converter(),
                new Decimal128ToBigDecimalConverter()
        ));
    }

    @Bean
    PaginationHelper paginationHelper(final PaginationConfig paginationConfig) {
        return new PaginationHelper(paginationConfig);
    }

    @Bean
    OpenApiConfig openApiConfig(final ApplicationConfig applicationConfig) {
        return new OpenApiConfig(applicationConfig);
    }

}
