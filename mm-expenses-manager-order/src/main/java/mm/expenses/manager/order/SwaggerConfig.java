package mm.expenses.manager.order;

import com.google.common.base.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeTypeUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Set;

import static com.google.common.base.Predicates.or;
import static mm.expenses.manager.order.UrlDefaultPaths.ORDER_URL;
import static mm.expenses.manager.order.UrlDefaultPaths.PRODUCT_URL;
import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
@SuppressWarnings("java:S4738")
class SwaggerConfig {

    @Value("${mm-expenses-manager.app-version}")
    private String version;

    @Value("${mm-expenses-manager.app-name}")
    private String applicationName;

    private static final Set<String> FORMAT_OF_DATA = Set.of(MimeTypeUtils.APPLICATION_JSON_VALUE);

    @Bean
    Docket defaultDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(AppPath.DEFAULT_ORDER)
                .select()
                .paths(AppPath.defaultPaths())
                .build()
                .enableUrlTemplating(true)
                .consumes(FORMAT_OF_DATA)
                .produces(FORMAT_OF_DATA)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo());
    }

    @Bean
    Docket productApi() {
        return buildDocket(AppPath.PRODUCT.getTitle(), AppPath.PRODUCT.getPath(), productInfo());
    }

    @Bean
    Docket orderApi() {
        return buildDocket(AppPath.ORDER.getTitle(), AppPath.ORDER.getPath(), orderInfo());
    }

    private ApiInfo apiInfo() {
        return buildInfo(" - all endpoints of service.");
    }

    private ApiInfo productInfo() {
        return buildInfo(" - endpoints of Product.");
    }

    private ApiInfo orderInfo() {
        return new ApiInfoBuilder()
                .title(applicationName)
                .description(applicationName + " - endpoints of Orders.")
                .version(version)
                .build();
    }

    private ApiInfo buildInfo(String description) {
        return new ApiInfoBuilder()
                .title(applicationName)
                .description(applicationName + description)
                .version(version)
                .build();
    }

    private Docket buildDocket(String groupName, Predicate<String> path, ApiInfo apiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(groupName)
                .select()
                .paths(path)
                .build()
                .enableUrlTemplating(true)
                .consumes(FORMAT_OF_DATA)
                .produces(FORMAT_OF_DATA)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo);
    }

    @Getter
    @RequiredArgsConstructor
    private enum AppPath {
        PRODUCT(prepareRegexOfUrl(PRODUCT_URL), "2) Product"),
        ORDER(prepareRegexOfUrl(ORDER_URL), "3) Order");

        static String DEFAULT_ORDER = "1) Default";

        final Predicate<String> path;

        final String title;

        static Predicate<String> defaultPaths() {
            return or(AppPath.PRODUCT.getPath(), AppPath.ORDER.getPath());
        }

        private static Predicate<String> prepareRegexOfUrl(final String url) {
            return regex(String.format("%s.*", url));
        }

    }

}
