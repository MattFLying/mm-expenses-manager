package mm.expenses.manager.order;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Generated
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@EnableJpaRepositories
@EnableConfigurationProperties
@SpringBootApplication
public class OrderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
