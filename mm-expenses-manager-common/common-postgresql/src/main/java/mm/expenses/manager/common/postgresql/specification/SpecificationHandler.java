package mm.expenses.manager.common.postgresql.specification;

import jakarta.persistence.criteria.*;
import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationParseException;
import mm.expenses.manager.common.postgresql.specification.criteria.*;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.utils.pagination.sort.SortProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines {@link Specification} handler for specific class type to allow to handle specification criteria
 * for specified type.
 *
 * @param <T> specific class type to be handled for handler
 */
public abstract class SpecificationHandler<T> {

    protected final CriteriaParameterConverters converters = new CriteriaParameterConverters();
    protected final JsonBCriteriaParameterConverter jsonBConverter = new JsonBCriteriaParameterConverter();

    /**
     * @return {@link SpecificationCriteria} for specified object type
     */
    public abstract SpecificationCriteria getSpecificationCriteria();

    /**
     * Defines specification criteria to be handled by JPA for specific data type.
     *
     * @param additionalCriteriaParameters additional criteria parameters that were not defined as query params but requires to be handled
     * @return result of specification
     */
    public HandledSpecificationResult<T> handle(final AdditionalCriteriaParameter... additionalCriteriaParameters) {
        return handleParameters(null, additionalCriteriaParameters);
    }

    /**
     * Defines specification criteria to be handled by JPA for specific data type.
     *
     * @param queryParameters              expected parameters defined in query to be handled
     * @param additionalCriteriaParameters additional criteria parameters that were not defined as query params but requires to be handled
     * @return result of specification
     */
    public HandledSpecificationResult<T> handle(final Map<String, String[]> queryParameters, final AdditionalCriteriaParameter... additionalCriteriaParameters) {
        return Objects.isNull(queryParameters)
                ? handleParameters(null, additionalCriteriaParameters)
                : handleParameters(
                queryParameters.entrySet()
                        .stream()
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue()[0]))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue)),
                additionalCriteriaParameters
        );
    }

    /**
     * @return additional {@link AdditionalPredicate} based on passed criteria parameters if needed. By default, there is no definition.
     */
    protected AdditionalPredicate<T> additionalPredicateDefinition() {
        return (criteriaParameters, root, query, builder) -> null;
    }

    /**
     * Creates predicate for specific field if this is a JsonB field type.
     */
    protected void handleJsonBParameter(final List<Predicate> predicates, final CriteriaBuilder builder, final From<?, ?> fromTable, final CriteriaParameter criteriaParameter, final String rootParameterName, final String expectedParameterName) {
        val predicate = jsonBConverter.convertToPredicateBasedOnFieldType(builder, fromTable, rootParameterName, expectedParameterName, criteriaParameter);
        if (Objects.nonNull(predicate)) {
            predicates.add(predicate);
        }
    }

    /**
     * Converts {@link CriteriaParameter} to {@link Predicate}.
     */
    protected Predicate convertCriteriaParameterToPredicate(final CriteriaParameter criteriaParameter, final Root<?> root, CriteriaQuery<?> query, final CriteriaBuilder builder) {
        val converter = converters.getConverter(criteriaParameter);
        if (Objects.isNull(converter)) {
            throw SpecificationParseException.converterNotFound(criteriaParameter);
        }
        return converter.restrict(criteriaParameter, root, builder);
    }

    /**
     * @return {@link HandledSpecificationResult} based on given parameters to be handled
     */
    private HandledSpecificationResult<T> handleParameters(final Map<String, String> queryParameters, final AdditionalCriteriaParameter... additionalCriteriaParameters) {
        val parameters = Objects.nonNull(queryParameters) ? new LinkedList<Map.Entry<String, String>>(queryParameters.entrySet()) : Collections.<Map.Entry<String, String>>emptyList();
        val criteria = getSpecificationCriteria();
        val pageable = definePageable(parameters, criteria);
        val specification = parseToSpecification(parameters, criteria, additionalCriteriaParameters);

        return new HandledSpecificationResult<>(specification, pageable);
    }

    /**
     * @return defines pagination for specification criteria based on queried parameters or default
     */
    private Pageable definePageable(final List<Map.Entry<String, String>> parameters, final SpecificationCriteria criteria) {
        var pageNumber = PaginationConfig.DEFAULT_PAGE_NUMBER;
        var pageSize = PaginationConfig.DEFAULT_PAGE_SIZE;

        val pagination = criteria.getPagination();
        if (Objects.nonNull(pagination)) {
            pageSize = pagination.getPageSize();
        }

        val orders = new LinkedList<Sort.Order>();
        var parametersIterator = parameters.iterator();
        while (parametersIterator.hasNext()) {
            var entry = parametersIterator.next();
            val key = entry.getKey();
            val value = entry.getValue();

            try {
                switch (key) {
                    case PaginationConfig.PAGE_NUMBER:
                        pageNumber = Integer.parseInt(value);
                        parametersIterator.remove();
                        break;
                    case PaginationConfig.PAGE_SIZE:
                        pageSize = limitPageSize(Integer.parseInt(value), pagination);
                        parametersIterator.remove();
                        break;
                    case PaginationConfig.SORT:
                        orders.addAll(parsePossibleSorting(value, criteria));
                        parametersIterator.remove();
                        break;
                }
            } catch (final NumberFormatException exception) {
                throw SpecificationParseException.incorrectValueFormatForField(key, value, exception);
            } catch (final SpecificationParseException exception) {
                throw exception;
            } catch (final Exception exception) {
                throw new SpecificationParseException(exception.getMessage(), exception);
            }
        }

        return PageRequest.of(
                pageNumber,
                pageSize,
                CollectionUtils.isNotEmpty(orders)
                        ? Sort.by(orders)
                        : Sort.unsorted()
        );
    }

    /**
     * @return expected page size or default max page size if the expected page size exceeded the max size limit
     */
    private int limitPageSize(final int pageSize, final PaginationConfig.Pagination pagination) {
        return Objects.nonNull(pagination) && pageSize > pagination.getMaxPageSize()
                ? pagination.getMaxPageSize()
                : pageSize;
    }

    /**
     * @return expected sorting or default
     */
    private List<Sort.Order> parsePossibleSorting(final String parameterValue, final SpecificationCriteria criteria) {
        val orders = new LinkedList<Sort.Order>();
        val valueParts = splitValueBySeparator(parameterValue);

        valueParts.forEach(valuePart -> {
            if (StringUtils.isNotEmpty(valuePart)) {
                val positionOfSeparator = valuePart.indexOf(Operation.SEPARATOR);
                val isSeparatorPresent = positionOfSeparator != -1;
                val fieldName = isSeparatorPresent
                        ? valuePart.substring(0, positionOfSeparator)
                        : valuePart;

                if (!criteria.canFieldBeSorted(fieldName)) {
                    throw SpecificationParseException.fieldCannotBeSorted(fieldName);
                }

                val sortDirection = isSeparatorPresent
                        ? SortProperty.SortDirection.of(valuePart.substring(positionOfSeparator + 1).trim())
                        : SortProperty.SortDirection.ASC;
                orders.add(new Sort.Order(Sort.Direction.fromString(sortDirection.toLowerCase()), fieldName));
            }
        });
        return orders;
    }

    private static List<String> splitValueBySeparator(final String values) {
        return Arrays.stream(
                        Objects.nonNull(values)
                                ? values.split(",")
                                : new String[0]
                )
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private Specification<T> parseToSpecification(final List<Map.Entry<String, String>> parameters, final SpecificationCriteria criteria, final AdditionalCriteriaParameter[] additionalCriteriaParameters) {
        try {
            val parsedParams = parameters.stream()
                    .map(entry -> new CriteriaParameter.CriteriaParameterBuilder(entry, criteria))
                    .map(CriteriaParameter.CriteriaParameterBuilder::build)
                    .collect(Collectors.toList());

            if (Objects.nonNull(additionalCriteriaParameters)) {
                Arrays.stream(additionalCriteriaParameters)
                        .forEach(additionalCriteriaParameter -> parsedParams.add(
                                SpecificationScanner.scanParameter(additionalCriteriaParameter, ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments())
                        ));
            }
            return parseCriteriaParametersToSpecification(parsedParams);
        } catch (final Exception exception) {
            throw new SpecificationParseException(exception.getMessage(), exception);
        }
    }

    private Specification<T> parseCriteriaParametersToSpecification(final List<CriteriaParameter> criteriaParameters) {
        return (root, query, builder) -> {
            val additionalPredicate = additionalPredicateDefinition().handle(
                    criteriaParameters.stream()
                            .filter(criteriaParameter -> !criteriaParameter.isStandard())
                            .toList(),
                    root,
                    query,
                    builder
            );
            val parsedParamsAsPredicates = criteriaParameters.stream()
                    .filter(CriteriaParameter::isStandard)
                    .map(param -> convertCriteriaParameterToPredicate(param, root, query, builder))
                    .filter(Objects::nonNull)
                    .toArray(Predicate[]::new);
            val standard = builder.and(parsedParamsAsPredicates);

            return Objects.nonNull(additionalPredicate)
                    ? builder.and(standard, additionalPredicate)
                    : standard;
        };
    }

}
