package mm.expenses.manager.finance;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Generated
@EnableCaching
@EnableScheduling
@EnableFeignClients
@EnableMongoRepositories
@SpringBootApplication
public class FinanceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }

}
