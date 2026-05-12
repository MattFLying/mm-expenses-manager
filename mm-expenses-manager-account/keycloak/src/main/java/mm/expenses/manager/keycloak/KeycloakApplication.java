package mm.expenses.manager.keycloak;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Generated
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
@SpringBootApplication
public class KeycloakApplication {

    public static void main(final String[] args) {
        SpringApplication.run(KeycloakApplication.class, args);
    }

}