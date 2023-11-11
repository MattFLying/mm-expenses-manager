package mm.expenses.manager.product.pageable;

import mm.expenses.manager.common.beans.pageable.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PageFactory {

    private static PaginationConfig config;

    @Bean
    Object initPaginationConfig(final PaginationConfig paginationConfig) {
        config = paginationConfig;
        return null;
    }

    public static PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize) {
        return PageHelper.getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), findCorrectSort(null), config.getMaxPageSize());
    }

    public static PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize, final Sort sort) {
        return PageHelper.getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), findCorrectSort(sort), config.getMaxPageSize());
    }

    private static Integer findCorrectPageSize(final Integer pageSize) {
        return Objects.isNull(pageSize) ? config.getDefaultPageSize() : pageSize;
    }

    private static Integer findCorrectPageNumber(final Integer pageNumber) {
        if (Objects.isNull(pageNumber)) {
            return config.getMinPageNumber();
        }
        return pageNumber >= config.getMinPageNumber() ? pageNumber : config.getMinPageNumber();
    }

    private static Sort findCorrectSort(final Sort sort) {
        return Objects.isNull(sort) ? Sort.unsorted() : sort;
    }

}
