package mm.expenses.manager.finance.pageable;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.pageable.PageHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PageFactory {

    private final PaginationConfig config;

    public PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize, final Sort sort) {
        return PageHelper.getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), findCorrectSort(sort), config.getMaxPageSize());
    }

    public PageRequest getPageRequest(final Integer pageNumber, final Integer pageSize) {
        return PageHelper.getPageRequest(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), config.getMaxPageSize());
    }

    public Pageable getPageable(final Integer pageNumber, final Integer pageSize) {
        return PageHelper.getPageable(findCorrectPageNumber(pageNumber), findCorrectPageSize(pageSize), config.getMaxPageSize());
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

    private Sort findCorrectSort(final Sort sort) {
        return Objects.isNull(sort) ? Sort.unsorted() : sort;
    }

}
