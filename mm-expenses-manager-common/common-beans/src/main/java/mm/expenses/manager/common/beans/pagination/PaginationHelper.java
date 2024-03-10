package mm.expenses.manager.common.beans.pagination;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.Objects;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
public final class PaginationHelper {

    private final PaginationConfig config;

    public PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize, final SortOrder sort) {
        return getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), findCorrectSort(sort), config.getMaxPageSize());
    }

    public PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize) {
        return getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), config.getMaxPageSize());
    }

    public Pageable getPageable(final Integer pageNumber, final Integer pageSize) {
        return getPageable(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), config.getMaxPageSize());
    }

    public boolean isPageNumberAndPageSizePresent(final Integer pageNumber, final Integer pageSize) {
        return (Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize));
    }

    private Pageable getPageable(final Integer page, final Integer size, final Integer maxPageSize) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size > 0)) {
            return pageRequestOf(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return pageRequestOf(0, maxPageSize);
        } else {
            throw new InvalidParameterException(format("Incorrect page number: %s or/and page size: %s", page, size));
        }
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

    private PageRequest getPageRequest(final Integer page, final Integer size, final Sort sort, final Integer maxPageSize) {
        if (Objects.isNull(sort)) {
            return getPageRequest(page, size, maxPageSize);
        }
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size > 0)) {
            return PageRequest.of(page, size, sort);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return PageRequest.of(0, maxPageSize, sort);
        } else {
            throw new InvalidParameterException(format("Incorrect page number: %s or/and page size: %s or/and sorting: %s", page, size, sort));
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

    private Sort findCorrectSort(final SortOrder sortOrder) {
        if (Objects.nonNull(sortOrder)) {
            final var sort = sortOrder.getSort();
            if (Objects.nonNull(sort)) {
                return sort;
            }
        }
        return SortOrder.unsorted();
    }

}
