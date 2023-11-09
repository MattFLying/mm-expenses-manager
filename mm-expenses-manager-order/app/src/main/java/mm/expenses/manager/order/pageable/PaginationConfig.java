package mm.expenses.manager.order.pageable;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration.pageable")
class PaginationConfig {

    private Integer defaultPageSize;
    private Integer maxPageSize;
    private Integer minPageNumber;

}
