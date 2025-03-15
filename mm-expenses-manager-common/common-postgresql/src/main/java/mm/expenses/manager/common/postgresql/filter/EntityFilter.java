package mm.expenses.manager.common.postgresql.filter;

import lombok.*;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents entity's filtering based on passed specific parameters and builds simple map
 * that represents expected values for expected parameters.
 */
@SuperBuilder(toBuilder = true)
@Getter(value = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PROTECTED)
public abstract class EntityFilter {

    public static final String FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE = "filter-by-explicit-query-only";

    public static final String IS_DELETED_PROPERTY = "isDeleted";

    public static final String GENERAL_QUERY_PROPERTY = "query";

    public static final String SHOULD_CONVERT_CURRENCY_PROPERTY = "shouldConvertCurrency";

    private String query;

    private Boolean isDeleted;
    private Boolean shouldConvertCurrency;

    private PageRequest paginationConfig;

    /**
     * @return builds query parameters based on passed filters.
     */
    public Map<String, String[]> buildQueryParams() {
        val queryParameters = new HashMap<String, String[]>();

        if (isExplicitQueryOriented()) {
            val params = query.split("&");
            for (val queryParameter : params) {
                val parameter = queryParameter.split("=");

                val parameterName = parameter[0];
                val parameterValues = parameter[1].split(",");

                queryParameters.put(parameterName, parameterValues);
            }
            return queryParameters;
        }
        buildSpecificQueryParameters(queryParameters);
        return queryParameters;
    }

    /**
     * @return checks if all currencies should be converted to the default currency or not.
     */
    public boolean shouldConvertCurrenciesToDefault() {
        return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
    }

    /**
     * @return checks if general query parameter is in use.
     */
    protected boolean isExplicitQueryOriented() {
        val isQueryPresent = StringUtils.isNotBlank(query);
        if (isQueryPresent) {
            val listOfSpecificFilters = getSpecificFiltersPresenceList();
            if (listOfSpecificFilters.stream().anyMatch(filter -> filter)) {
                throw new ApiBadRequestException(new ExceptionType() {
                    @Override
                    public String getCode() {
                        return FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE;
                    }

                    @Override
                    public String getMessage() {
                        return SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY;
                    }

                    @Override
                    public ExceptionType withParameters(Object... params) {
                        return this;
                    }
                });
            }
            return true;
        }
        return false;
    }

    /**
     * Builds pagination parameters based on the pagination configuration.
     */
    protected void buildPagination(final Map<String, String[]> queryParameters) {
        if (Objects.nonNull(paginationConfig)) {
            queryParameters.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(paginationConfig.getPageNumber())});
            queryParameters.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(paginationConfig.getPageSize())});
        }
    }

    /**
     * Builds deleted parameter.
     */
    protected void buildDeleted(final Map<String, String[]> queryParameters) {
        if (shouldBeDeleted()) {
            queryParameters.put(IS_DELETED_PROPERTY, new String[]{String.valueOf(isDeleted)});
        }
    }

    /**
     * @return list of presence of specific query parameters available during filtering of specific entity.
     */
    protected abstract List<Boolean> getSpecificFiltersPresenceList();

    /**
     * build query parameters based on specific query parameters not defined in general query path variable.
     */
    protected abstract void buildSpecificQueryParameters(final Map<String, String[]> queryParameters);

    /**
     * @return checks if it should filter only be deleted products.
     */
    protected boolean shouldBeDeleted() {
        return Objects.nonNull(isDeleted) && isDeleted;
    }

}
