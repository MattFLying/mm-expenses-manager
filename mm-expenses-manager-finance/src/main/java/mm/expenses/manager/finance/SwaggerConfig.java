package mm.expenses.manager.finance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeTypeUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Set;

@EnableSwagger2
@Configuration
@SuppressWarnings("java:S4738")
class SwaggerConfig {

    @Value("${mm-expenses-manager-finance.app-version}")
    private String version;

    @Value("${mm-expenses-manager-finance.app-name}")
    private String applicationName;

    private static final Set<String> FORMAT_OF_DATA = Set.of(MimeTypeUtils.APPLICATION_JSON_VALUE);

    @Bean
    Docket defaultDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .build()
                .enableUrlTemplating(true)
                .consumes(FORMAT_OF_DATA)
                .produces(FORMAT_OF_DATA)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo());
    }

    @Bean
    public UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(applicationName)
                .version(version)
                .build();
    }

}
