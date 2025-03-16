package mm.expenses.manager.common.postgresql.pagination;

import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.Objects;

import static java.lang.String.format;

@Component
public final class PaginationHelper {

    private final PaginationConfig config;

    public PaginationHelper(final PaginationConfig config) {
        this.config = config;
    }

    public PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize) {
        return getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), config.getMaxPageSize());
    }

    private PageRequest getPageRequest(final Integer page, final Integer size, final Integer maxPageSize) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size > 0)) {
            return pageRequestOf(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return pageRequestOf(0, maxPageSize);
        } else {
            throw new InvalidParameterException(format("Incorrect page number: %s or/and page size: %s", page, size));
        }
    }

    private PageRequest pageRequestOf(final int number, final int size) {
        return PageRequest.of(number, size);
    }

    private Integer findCorrectPageSize(final Integer pageSize) {
        return Objects.isNull(pageSize) ? config.getDefaultPageSize() : pageSize;
    }

    private Integer findCorrectPageNumber(final Integer pageNumber) {
        if (Objects.isNull(pageNumber)) {
            return config.getMinPageNumber();
        }
        return pageNumber >= config.getMinPageNumber() ? pageNumber : config.getMinPageNumber();
    }

}
