package mm.expenses.manager.order;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI();
    }

}