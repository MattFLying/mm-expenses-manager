package mm.expenses.manager.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@EnableConfigurationProperties
@SpringBootApplication
public class OrderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
