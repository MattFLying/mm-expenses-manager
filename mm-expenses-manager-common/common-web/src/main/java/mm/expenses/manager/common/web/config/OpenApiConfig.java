package mm.expenses.manager.common.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final ApplicationConfigProperties applicationConfig;

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title(applicationConfig.getName())
                .description(applicationConfig.getDescription())
                .version(applicationConfig.getVersion())
                .contact(buildContact())
        );
    }

    private Contact buildContact() {
        return new Contact().name(applicationConfig.getDeveloperNameWithRole()).email(applicationConfig.getDeveloperEmail());
    }

}
