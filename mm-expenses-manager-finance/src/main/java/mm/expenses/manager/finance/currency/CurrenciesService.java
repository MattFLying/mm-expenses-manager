package mm.expenses.manager.finance.currency;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
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
public class CurrenciesService {

    @Getter(value = AccessLevel.PRIVATE)
    private final CurrencyProviders currencyProviders;

    private Collection<String> allAvailableCurrencyCodes;

    private Collection<String> availableCurrencyCodes;

    private Collection<CurrencyCode> allAvailableCurrencies;

    private Collection<CurrencyCode> availableCurrencies;

    private int allAvailableCurrenciesCount;

    private int availableCurrenciesCount;

    @PostConstruct
    void initialize() {
        this.allAvailableCurrencyCodes = getAllAvailableCurrenciesWithDefault().stream().map(CurrencyCode::getCode).sorted().collect(Collectors.toUnmodifiableList());
        this.allAvailableCurrencies = getAllAvailableCurrenciesWithDefault().stream().sorted(Comparator.comparing(CurrencyCode::getCode)).collect(Collectors.toUnmodifiableList());
        this.allAvailableCurrenciesCount = allAvailableCurrencyCodes.size();

        this.availableCurrencyCodes = getAllAvailableCurrenciesWithoutDefault().stream().map(CurrencyCode::getCode).sorted().collect(Collectors.toUnmodifiableList());
        this.availableCurrencies = getAllAvailableCurrenciesWithoutDefault().stream().sorted(Comparator.comparing(CurrencyCode::getCode)).collect(Collectors.toUnmodifiableList());
        this.availableCurrenciesCount = availableCurrencyCodes.size();
    }

    public CurrencyCode getCurrentCurrency() {
        return currencyProviders.getCurrency();
    }

    public Collection<CurrencyCode> getAllAvailableCurrenciesWithoutDefault() {
        return Stream.of(CurrencyCode.values())
                .filter(code -> !code.equals(CurrencyCode.UNDEFINED) && !code.equals(getCurrentCurrency()))
                .collect(Collectors.toList());
    }

    public Collection<CurrencyCode> getAllAvailableCurrenciesWithDefault() {
        return Stream.of(CurrencyCode.values())
                .filter(code -> !code.equals(CurrencyCode.UNDEFINED))
                .collect(Collectors.toList());
    }

    /**
     * Validate if given currency code is different than unknown and currently used in system.
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

}
