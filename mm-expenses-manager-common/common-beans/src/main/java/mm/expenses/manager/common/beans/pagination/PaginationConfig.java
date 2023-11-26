package mm.expenses.manager.common.beans.pagination;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration.pagination")
public class PaginationConfig {

    public static final String PAGE_NUMBER_PROPERTY = "pageNumber";
    public static final String PAGE_SIZE_PROPERTY = "pageSize";

    private Integer defaultPageSize = 1;
    private Integer maxPageSize = 0;
    private Integer minPageNumber = 0;

}
