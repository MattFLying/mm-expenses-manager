package mm.expenses.manager.exception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "mm.expenses.manager")
@SpringBootApplication
public class ApplicationException {

    public static void main(final String[] args) {
        SpringApplication.run(ApplicationException.class, args);
    }

}
