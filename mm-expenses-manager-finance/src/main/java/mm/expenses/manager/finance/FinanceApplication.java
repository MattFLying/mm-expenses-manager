package mm.expenses.manager.finance;

import mm.expenses.manager.ErrorHandlingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@EnableMongoRepositories
@SpringBootApplication
@Import({ErrorHandlingConfig.class})
public class FinanceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }

}
