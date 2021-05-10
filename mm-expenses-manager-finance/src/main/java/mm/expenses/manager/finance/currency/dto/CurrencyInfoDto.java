package mm.expenses.manager.finance.currency.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import mm.expenses.manager.common.i18n.CountryCode;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Schema(name = "CurrencyInfoDto", description = "Currency codes information response.")
@Getter
@JsonPropertyOrder({"currencies"})
public class CurrencyInfoDto {

    @Schema(description = "Available currency codes.")
    private final Collection<CurrencyCodeDto> currencies;

    public CurrencyInfoDto(final Collection<CurrencyCode> currenciesCodes) {
        this.currencies = currenciesCodes.stream().map(CurrencyCodeDto::new).sorted(Comparator.comparing(CurrencyCodeDto::getCode)).collect(Collectors.toList());
    }

    @Schema(name = "CurrencyCodeDto", description = "Currency code response.")
    @Getter
    @JsonPropertyOrder({"code", "name", "isoCode", "usedInCountries"})
    public static class CurrencyCodeDto {

        @Schema(description = "Currency code.")
        private final String code;

        @Schema(description = "Currency name.")
        private final String name;

        @Schema(description = "Currency ISO 4217 code.")
        private final Integer isoCode;

        @Schema(description = "Countries where currency is used.")
        private final Collection<CountryDto> usedInCountries;

        public CurrencyCodeDto(final CurrencyCode currencyCode) {
            this.code = currencyCode.getCode();
            this.name = currencyCode.getName();
            this.isoCode = currencyCode.getIsoCode();
            this.usedInCountries = currencyCode.getCountryCodesList()
                    .stream()
                    .map(CountryDto::new)
                    .sorted(Comparator.comparing(CountryDto::getCode))
                    .collect(Collectors.toList());
        }

        @Schema(name = "CountryDto", description = "Country response.")
        @Getter
        @JsonPropertyOrder({"code", "name"})
        public static class CountryDto {

            @Schema(description = "Country two digits code.")
            private final String code;

            @Schema(description = "Country name.")
            private final String name;

            public CountryDto(final CountryCode countryCode) {
                this.code = countryCode.getCode();
                this.name = countryCode.getName();
            }

        }

    }

}
