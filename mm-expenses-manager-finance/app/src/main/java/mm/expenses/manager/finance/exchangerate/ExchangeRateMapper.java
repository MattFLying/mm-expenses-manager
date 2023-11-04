package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.exchangerate.model.*;
import mm.expenses.manager.finance.config.MapperImplNaming;
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

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.EXCHANGE_RATE_MAPPER)
abstract class ExchangeRateMapper extends AbstractMapper {

    @Autowired
    protected CurrencyProviders providers;

    abstract RateDto map(final Rate rate);

    @Mapping(target = "date", expression = "java(fromInstantToLocalDate(exchangeRate.getDate()))")
    @Mapping(target = "rate", expression = "java(map(exchangeRate.getRateByProvider(providers.getProviderName(), true)))")
    abstract ExchangeRateDto mapToDto(final ExchangeRate exchangeRate);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "ratesByProvider", expression = "java(map(domain, providers.getProviderName()))")
    @Mapping(target = "detailsByProvider", expression = "java(map(providers.getProviderName(), domain))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "modifiedAt", source = "now")
    abstract ExchangeRate map(final CurrencyRate domain, final Instant now);

    @Mapping(target = "currency", expression = "java(currency.getCode())")
    @Mapping(target = "rates", expression = "java(exchangeRates.stream().map(this::mapToDto).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final CurrencyCode currency, final Collection<ExchangeRate> exchangeRates);

    @Mapping(target = "currency", expression = "java(exchangeRate.getCurrency().getCode())")
    @Mapping(target = "rates", expression = "java(java.util.stream.Stream.of(exchangeRate).map(this::mapToDto).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final ExchangeRate exchangeRate);

    public ExchangeRatesAccumulatePage mapAccumulatePage(List<ExchangeRatePage> content) {
        return mapAccumulatePage(
                content,
                content.size(),
                content.stream().mapToInt(page -> page.getContent().getRates().size()).sum(),
                content.stream().mapToLong(ExchangeRatePage::getTotalElements).sum()
        );
    }

    abstract ExchangeRatesAccumulatePage mapAccumulatePage(List<ExchangeRatePage> content, Integer currencies, Integer exchangeRates, Long totalExchangeRates);

    public ExchangeRatesPage map(final List<ExchangeRatesDto> content) {
        return map(content, content.size());
    }

    abstract ExchangeRatesPage map(final List<ExchangeRatesDto> content, Integer currencies);

    protected Map<String, Map<String, Object>> map(final String providerName, final CurrencyRate domain) {
        return new HashMap<>(Map.of(providerName, domain.getDetails()));
    }

    protected Map<String, Rate> map(final CurrencyRate domain, final String providerName) {
        return new HashMap<>(Map.of(providerName, map(domain.getCurrency(), providers.getCurrency(), domain.getRate())));
    }

    protected Rate map(final CurrencyCode currencyFrom, final CurrencyCode currencyTo, final Double currencyValueTo) {
        return Rate.of(currencyFrom, currencyTo, currencyValueTo);
    }

    protected List<ExchangeRatePage> groupAndSortPagedResult(final Stream<Page<ExchangeRate>> result) {
        return result
                .map(this::mapToExchangeRatePage)
                .filter(page -> !page.getEmpty())
                .sorted(Comparator.comparing(page -> page.getContent().getCurrency()))
                .collect(Collectors.toList());
    }

    private ExchangeRatePage mapToExchangeRatePage(final Page<ExchangeRate> page) {
        var exchangeRatePage = new ExchangeRatePage();
        exchangeRatePage.setContent(map(getCurrencyForPage(page), sortByDateByTheNewest(page.getContent())));
        exchangeRatePage.setPage(page.getNumber());
        exchangeRatePage.setElements(page.getNumberOfElements());
        exchangeRatePage.setEmpty(page.isEmpty());
        exchangeRatePage.setFirst(page.isFirst());
        exchangeRatePage.setLast(page.isLast());
        exchangeRatePage.setHasNext(page.hasNext());
        exchangeRatePage.setTotalPages(page.getTotalPages());
        exchangeRatePage.setTotalElements(page.getTotalElements());

        return exchangeRatePage;
    }

    private CurrencyCode getCurrencyForPage(final Page<ExchangeRate> page) {
        return page.getContent().stream().findFirst().map(ExchangeRate::getCurrency).orElse(CurrencyCode.UNDEFINED);
    }

    private Collection<ExchangeRate> sortByDateByTheNewest(final Collection<ExchangeRate> exchangeRates) {
        return exchangeRates.stream().sorted(Comparator.comparing(ExchangeRate::getDate).reversed()).collect(Collectors.toList());
    }

}

