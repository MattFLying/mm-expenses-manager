package mm.expenses.manager.finance.currency;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class CurrenciesService implements CurrencyProvider {

    @Getter(value = AccessLevel.PRIVATE)
    private final CurrencyProviders currencyProviders;

    private final CurrencyMapper mapper;

    private Collection<String> allAvailableCurrencyCodes;

    private Collection<String> availableCurrencyCodes;

    private Collection<CurrencyCode> allAvailableCurrencies;

    private Collection<CurrencyCode> availableCurrencies;

    private int allAvailableCurrenciesCount;

    private int availableCurrenciesCount;

    @PostConstruct
    void initialize() {
        this.allAvailableCurrencyCodes = filterAllWithDefault().stream().map(CurrencyCode::getCode).sorted().collect(Collectors.toUnmodifiableList());
        this.allAvailableCurrencies = filterAllWithDefault().stream().sorted(Comparator.comparing(CurrencyCode::getCode)).collect(Collectors.toUnmodifiableList());
        this.allAvailableCurrenciesCount = allAvailableCurrencyCodes.size();

        this.availableCurrencyCodes = filterAllWithoutDefault().stream().map(CurrencyCode::getCode).sorted().collect(Collectors.toUnmodifiableList());
        this.availableCurrencies = filterAllWithoutDefault().stream().sorted(Comparator.comparing(CurrencyCode::getCode)).collect(Collectors.toUnmodifiableList());
        this.availableCurrenciesCount = availableCurrencyCodes.size();
    }

    @Override
    public CurrencyCode getCurrentCurrency() {
        return currencyProviders.getCurrency();
    }

    /**
     * Validate if given currency code is different than unknown and currently used in system.
     *
     * @param currencyCode currency to be validated
     */
    public void validateIfCurrencyIsCorrect(final CurrencyCode currencyCode) {
        if (currencyCode.equals(CurrencyCode.UNDEFINED)) {
            throw new ApiBadRequestException(FinanceExceptionMessage.CURRENCY_NOT_ALLOWED);
        }
        if (currencyCode.equals(getCurrentCurrency())) {
            throw new ApiBadRequestException(FinanceExceptionMessage.DEFAULT_CURRENCY_NOT_ALLOWED);
        }
    }

    private Collection<CurrencyCode> filterAllWithoutDefault() {
        return Stream.of(CurrencyCode.values())
                .filter(code -> !code.equals(CurrencyCode.UNDEFINED) && !code.equals(getCurrentCurrency()))
                .collect(Collectors.toList());
    }

    private Collection<CurrencyCode> filterAllWithDefault() {
        return Stream.of(CurrencyCode.values())
                .filter(code -> !code.equals(CurrencyCode.UNDEFINED))
                .collect(Collectors.toList());
    }

}
