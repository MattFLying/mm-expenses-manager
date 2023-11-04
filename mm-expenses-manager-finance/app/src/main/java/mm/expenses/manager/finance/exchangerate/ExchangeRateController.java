package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.exception.api.ApiNotFoundException;
import mm.expenses.manager.finance.api.exchangerate.ExchangeRatesApi;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRatesAccumulatePage;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRatesDto;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRatesPage;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheMapper;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestRatesCacheService;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.pageable.PageFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("exchange-rates")
class ExchangeRateController implements ExchangeRatesApi {

    private final ExchangeRateService service;
    private final LatestRatesCacheService latest;
    private final ExchangeRateMapper mapper;
    private final ExchangeRateCacheMapper cacheMapper;
    private final PageFactory pageFactory;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ExchangeRatesAccumulatePage findAllExchangeRates(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                            @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (Objects.nonNull(date) && (Objects.nonNull(from) || Objects.nonNull(to))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_FILTER_BY_DATE_OR_DATE_RANGE_ONLY);
        }
        if ((Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED);
        }
        final var pageable = pageFactory.getPageable(pageNumber, pageSize);

        return mapper.mapAccumulatePage(mapper.groupAndSortPagedResult(service.findAll(date, from, to, pageable)));
    }

    @Override
    @GetMapping(value = "/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExchangeRatesAccumulatePage findAllForCurrency(@PathVariable("currency") String currency,
                                                          @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                          @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                          @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        if ((Objects.isNull(from) && Objects.nonNull(to)) || (Objects.nonNull(from) && Objects.isNull(to))) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_FILTER_BY_DATE_RANGE);
        }
        final var pageable = pageFactory.getPageable(pageNumber, pageSize);

        return mapper.mapAccumulatePage(mapper.groupAndSortPagedResult(service.findAllForCurrency(currencyCode, from, to, pageable)));
    }

    @Override
    @GetMapping(value = "/{currency}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExchangeRatesDto findLatestForCurrency(@PathVariable("currency") String currency) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        return latest.getLatest(currencyCode)
                .map(cacheMapper::mapCache)
                .orElseThrow(() -> new ApiNotFoundException(FinanceExceptionMessage.LATEST_CURRENCY_FOR_CODE_NOT_FOUND.withParameters(currencyCode.getCode())));
    }

    @Override
    @GetMapping(value = "/{currency}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExchangeRatesDto findForCurrencyAndDate(@PathVariable("currency") String currency,
                                                   @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        final var currencyCode = CurrencyCode.getCurrencyFromString(currency, false);
        return service.findForCurrencyAndSpecificDate(currencyCode, date)
                .map(mapper::map)
                .orElseThrow(() -> new ApiNotFoundException(FinanceExceptionMessage.CURRENCY_FOR_CODE_AND_DATE_NOT_FOUND.withParameters(currencyCode.getCode(), date)));
    }

    @Override
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExchangeRatesPage findLatest() {
        return mapper.map(cacheMapper.groupAndSortResultCache(latest.getLatest().stream()));
    }

}
