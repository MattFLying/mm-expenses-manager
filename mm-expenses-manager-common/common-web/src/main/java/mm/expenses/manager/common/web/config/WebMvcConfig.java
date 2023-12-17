package mm.expenses.manager.common.web.config;

import mm.expenses.manager.common.web.api.WebHttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods(allowedCorsHttpMethods())
                .maxAge(3600)
                .allowedOrigins("*")
                .allowedHeaders("*");
    }

    private String[] allowedCorsHttpMethods() {
        return Arrays.stream(WebHttpMethod.values()).map(Enum::name).toArray(String[]::new);
    }

}
