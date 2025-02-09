package mm.expenses.manager.common.utils.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration.pagination")
public class PaginationConfig {

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_MAX_PAGE_SIZE = 20;

    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PAGE_SIZE = "pageSize";
    public static final String SORT = "sort";

    // to delete later
    public static final String SORT_ORDER_PROPERTY = "sortOrder";
    // to delete later
    public static final String SORT_DESC_PROPERTY = "sortDesc";

    private Integer defaultPageSize = DEFAULT_PAGE_SIZE;
    private Integer minPageNumber = DEFAULT_PAGE_NUMBER;
    private Integer maxPageSize = DEFAULT_MAX_PAGE_SIZE;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class Pagination {

        private int pageNumber;

        private int pageSize;

        private int maxPageSize;

        public static Pagination of(final int defaultPageSize, final int maxPageSize) {
            return Pagination.builder()
                    .pageNumber(DEFAULT_PAGE_NUMBER)
                    .pageSize(defaultPageSize)
                    .maxPageSize(maxPageSize)
                    .build();
        }

        public static Pagination of(final int minPageNumber, final int defaultPageSize, final int maxPageSize) {
            return Pagination.builder()
                    .pageNumber(minPageNumber)
                    .pageSize(defaultPageSize)
                    .maxPageSize(maxPageSize)
                    .build();
        }

    }

}
