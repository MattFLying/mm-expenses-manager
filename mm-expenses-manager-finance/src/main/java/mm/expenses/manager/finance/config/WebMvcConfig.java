package mm.expenses.manager.finance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods(allowedCorsHttpMethods())
                .maxAge(3600)
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedHeaders("*");
    }

    private String[] allowedCorsHttpMethods() {
        return new String[]{
                HttpMethod.DELETE.name(), HttpMethod.GET.name(), HttpMethod.OPTIONS.name(),
                HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name()
        };
    }

}
