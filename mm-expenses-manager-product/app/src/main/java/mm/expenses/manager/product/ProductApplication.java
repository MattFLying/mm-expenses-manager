package mm.expenses.manager.product;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Generated
@EnableScheduling
@EnableFeignClients
@EnableMongoRepositories
@EnableConfigurationProperties
@SpringBootApplication
public class ProductApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
