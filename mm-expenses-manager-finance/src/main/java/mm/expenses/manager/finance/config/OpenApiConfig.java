package mm.expenses.manager.finance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class OpenApiConfig {

    private final AppConfigProperty appConfigProperty;

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title(appConfigProperty.getName())
                .description(appConfigProperty.getDescription())
                .version(appConfigProperty.getVersion())
                .contact(buildContact())
        );
    }

    private Contact buildContact() {
        return new Contact().name(appConfigProperty.getDeveloperNameWithRole()).email(appConfigProperty.getDeveloperEmail());
    }

}
