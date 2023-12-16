package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.finance.api.exchangerate.model.*;
import mm.expenses.manager.finance.exchangerate.ExchangeRate.Rate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, List.class, DateUtils.class, Map.class, HashMap.class}
)
public abstract class ExchangeRateMapper implements AbstractMapper {

    @Autowired
    protected CurrencyProviders providers;

    abstract RateDto map(final Rate rate);

    @Mapping(target = "date", expression = "java(DateUtils.instantToLocalDate(exchangeRate.getDate()))")
    @Mapping(target = "rate", expression = "java(map(exchangeRate.getRateByProvider(providers.getProviderName(), true)))")
    abstract ExchangeRateDto mapToDto(final ExchangeRate exchangeRate);

    @Mapping(target = "date", expression = "java(DateUtils.localDateToInstant(domain.getDate()))")
    @Mapping(target = "ratesByProvider", expression = "java(new HashMap<>(Map.of(providers.getProviderName(), map(domain.getCurrency(), providers.getCurrency(), domain.getRate()))))")
    @Mapping(target = "detailsByProvider", expression = "java(new HashMap<>(Map.of(providers.getProviderName(), domain.getDetails())))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "modifiedAt", source = "now")
    abstract ExchangeRate map(final CurrencyRate domain, final Instant now);

    @Mapping(target = "currency", expression = "java(currency.getCode())")
    @Mapping(target = "rates", expression = "java(exchangeRates.stream().map(this::mapToDto).collect(Collectors.toList()))")
    abstract ExchangeRatesDto map(final CurrencyCode currency, final Collection<ExchangeRate> exchangeRates);

    @Mapping(target = "currency", expression = "java(exchangeRate.getCurrency().getCode())")
    @Mapping(target = "rates", expression = "java(List.of(mapToDto(exchangeRate)))")
    abstract ExchangeRatesDto map(final ExchangeRate exchangeRate);

    @Mapping(target = "content", expression = "java(map(getCurrencyForPage(page), sortByDateByTheNewest(page.getContent())))")
    @Mapping(target = "hasNext", expression = "java(page.hasNext())")
    @Mapping(target = "elements", source = "page.numberOfElements")
    abstract ExchangeRatePage mapToPageResponse(final Page<ExchangeRate> page);

    abstract ExchangeRatesAccumulatePage mapAccumulatePage(final List<ExchangeRatePage> content, final Integer currencies, final Integer exchangeRates, final Long totalExchangeRates);

    abstract ExchangeRatesPage map(final List<ExchangeRatesDto> content, Integer currencies);

    public ExchangeRatesAccumulatePage mapAccumulatePage(final List<ExchangeRatePage> content) {
        return mapAccumulatePage(
                content,
                content.size(),
                content.stream().mapToInt(page -> page.getContent().getRates().size()).sum(),
                content.stream().mapToLong(ExchangeRatePage::getTotalElements).sum()
        );
    }

    public ExchangeRatesPage map(final List<ExchangeRatesDto> content) {
        return map(content, content.size());
    }

    protected Rate map(final CurrencyCode currencyFrom, final CurrencyCode currencyTo, final Double currencyValueTo) {
        return Rate.of(currencyFrom, currencyTo, currencyValueTo);
    }

    protected List<ExchangeRatePage> groupAndSortPagedResult(final Stream<Page<ExchangeRate>> result) {
        return result
                .map(this::mapToPageResponse)
                .filter(page -> !page.getEmpty())
                .sorted(Comparator.comparing(page -> page.getContent().getCurrency()))
                .collect(Collectors.toList());
    }

    protected CurrencyCode getCurrencyForPage(final Page<ExchangeRate> page) {
        return page.getContent().stream().findFirst().map(ExchangeRate::getCurrency).orElse(CurrencyCode.UNDEFINED);
    }

    protected Collection<ExchangeRate> sortByDateByTheNewest(final Collection<ExchangeRate> exchangeRates) {
        return exchangeRates.stream().sorted(Comparator.comparing(ExchangeRate::getDate).reversed()).collect(Collectors.toList());
    }

}

