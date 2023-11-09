package mm.expenses.manager.finance.currency;

import mm.expenses.manager.common.utils.i18n.CountryCode;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.FinanceApplicationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CurrenciesControllerTest extends FinanceApplicationTest {

    private static final String BASE_URL = "/currencies";
    private static final String INFO_URL = BASE_URL + "/info";
    private static final String CURRENT_URL = BASE_URL + "/current";

    @Autowired
    private CurrenciesService currenciesService;

    @Nested
    class FindAllAvailableCurrencyCodes {

        @Test
        void shouldRetrieveAllAvailableCurrencyCodes() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currenciesCount", is(currenciesService.getAllAvailableCurrenciesCount())))
                    .andExpect(jsonPath("$.codes[0]", is(CurrencyCode.AUD.getCode())))
                    .andExpect(jsonPath("$.codes[1]", is(CurrencyCode.CAD.getCode())))
                    .andExpect(jsonPath("$.codes[2]", is(CurrencyCode.CHF.getCode())))
                    .andExpect(jsonPath("$.codes[3]", is(CurrencyCode.EUR.getCode())))
                    .andExpect(jsonPath("$.codes[4]", is(CurrencyCode.GBP.getCode())))
                    .andExpect(jsonPath("$.codes[5]", is(CurrencyCode.JPY.getCode())))
                    .andExpect(jsonPath("$.codes[6]", is(CurrencyCode.NZD.getCode())))
                    .andExpect(jsonPath("$.codes[7]", is(CurrencyCode.PLN.getCode())))
                    .andExpect(jsonPath("$.codes[8]", is(CurrencyCode.SEK.getCode())))
                    .andExpect(jsonPath("$.codes[9]", is(CurrencyCode.USD.getCode())));
        }

    }

    @Nested
    class FindCurrentCurrencyCode {

        @Test
        void shouldRetrieveCurrentCurrencyCode() throws Exception {
            mockMvc.perform(get(CURRENT_URL))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currenciesCount", is(1)))
                    .andExpect(jsonPath("$.codes[0]", is(CurrencyCode.PLN.getCode())));
        }

    }

    @Nested
    class FindAllAvailableCurrenciesInformation {

        @Test
        void shouldRetrieveAllAvailableCurrenciesInformation() throws Exception {
            mockMvc.perform(get(INFO_URL))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currencies", hasSize(currenciesService.getAllAvailableCurrenciesCount())))

                    .andExpect(jsonPath("$.currencies[0].code", is(CurrencyCode.AUD.getCode())))
                    .andExpect(jsonPath("$.currencies[0].name", is(CurrencyCode.AUD.getName())))
                    .andExpect(jsonPath("$.currencies[0].isoCode", is(CurrencyCode.AUD.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[0].usedInCountries", hasSize(CurrencyCode.AUD.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[0].usedInCountries[0].code", is(CountryCode.AU.getCode())))
                    .andExpect(jsonPath("$.currencies[0].usedInCountries[0].name", is(CountryCode.AU.getName())))

                    .andExpect(jsonPath("$.currencies[1].code", is(CurrencyCode.CAD.getCode())))
                    .andExpect(jsonPath("$.currencies[1].name", is(CurrencyCode.CAD.getName())))
                    .andExpect(jsonPath("$.currencies[1].isoCode", is(CurrencyCode.CAD.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[1].usedInCountries", hasSize(CurrencyCode.CAD.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[1].usedInCountries[0].code", is(CountryCode.CA.getCode())))
                    .andExpect(jsonPath("$.currencies[1].usedInCountries[0].name", is(CountryCode.CA.getName())))

                    .andExpect(jsonPath("$.currencies[2].code", is(CurrencyCode.CHF.getCode())))
                    .andExpect(jsonPath("$.currencies[2].name", is(CurrencyCode.CHF.getName())))
                    .andExpect(jsonPath("$.currencies[2].isoCode", is(CurrencyCode.CHF.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[2].usedInCountries", hasSize(CurrencyCode.CHF.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[2].usedInCountries[0].code", is(CountryCode.CH.getCode())))
                    .andExpect(jsonPath("$.currencies[2].usedInCountries[0].name", is(CountryCode.CH.getName())))

                    .andExpect(jsonPath("$.currencies[3].code", is(CurrencyCode.EUR.getCode())))
                    .andExpect(jsonPath("$.currencies[3].name", is(CurrencyCode.EUR.getName())))
                    .andExpect(jsonPath("$.currencies[3].isoCode", is(CurrencyCode.EUR.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[3].usedInCountries", hasSize(CurrencyCode.EUR.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[3].usedInCountries[0].code", is(CountryCode.AD.getCode())))
                    .andExpect(jsonPath("$.currencies[3].usedInCountries[0].name", is(CountryCode.AD.getName())))

                    .andExpect(jsonPath("$.currencies[4].code", is(CurrencyCode.GBP.getCode())))
                    .andExpect(jsonPath("$.currencies[4].name", is(CurrencyCode.GBP.getName())))
                    .andExpect(jsonPath("$.currencies[4].isoCode", is(CurrencyCode.GBP.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[4].usedInCountries", hasSize(CurrencyCode.GBP.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[4].usedInCountries[0].code", is(CountryCode.GB.getCode())))
                    .andExpect(jsonPath("$.currencies[4].usedInCountries[0].name", is(CountryCode.GB.getName())))

                    .andExpect(jsonPath("$.currencies[5].code", is(CurrencyCode.JPY.getCode())))
                    .andExpect(jsonPath("$.currencies[5].name", is(CurrencyCode.JPY.getName())))
                    .andExpect(jsonPath("$.currencies[5].isoCode", is(CurrencyCode.JPY.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[5].usedInCountries", hasSize(CurrencyCode.JPY.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[5].usedInCountries[0].code", is(CountryCode.JP.getCode())))
                    .andExpect(jsonPath("$.currencies[5].usedInCountries[0].name", is(CountryCode.JP.getName())))

                    .andExpect(jsonPath("$.currencies[6].code", is(CurrencyCode.NZD.getCode())))
                    .andExpect(jsonPath("$.currencies[6].name", is(CurrencyCode.NZD.getName())))
                    .andExpect(jsonPath("$.currencies[6].isoCode", is(CurrencyCode.NZD.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[6].usedInCountries", hasSize(CurrencyCode.NZD.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[6].usedInCountries[0].code", is(CountryCode.CK.getCode())))
                    .andExpect(jsonPath("$.currencies[6].usedInCountries[0].name", is(CountryCode.CK.getName())))

                    .andExpect(jsonPath("$.currencies[7].code", is(CurrencyCode.PLN.getCode())))
                    .andExpect(jsonPath("$.currencies[7].name", is(CurrencyCode.PLN.getName())))
                    .andExpect(jsonPath("$.currencies[7].isoCode", is(CurrencyCode.PLN.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[7].usedInCountries", hasSize(CurrencyCode.PLN.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[7].usedInCountries[0].code", is(CountryCode.PL.getCode())))
                    .andExpect(jsonPath("$.currencies[7].usedInCountries[0].name", is(CountryCode.PL.getName())))

                    .andExpect(jsonPath("$.currencies[8].code", is(CurrencyCode.SEK.getCode())))
                    .andExpect(jsonPath("$.currencies[8].name", is(CurrencyCode.SEK.getName())))
                    .andExpect(jsonPath("$.currencies[8].isoCode", is(CurrencyCode.SEK.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[8].usedInCountries", hasSize(CurrencyCode.SEK.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[8].usedInCountries[0].code", is(CountryCode.SE.getCode())))
                    .andExpect(jsonPath("$.currencies[8].usedInCountries[0].name", is(CountryCode.SE.getName())))

                    .andExpect(jsonPath("$.currencies[9].code", is(CurrencyCode.USD.getCode())))
                    .andExpect(jsonPath("$.currencies[9].name", is(CurrencyCode.USD.getName())))
                    .andExpect(jsonPath("$.currencies[9].isoCode", is(CurrencyCode.USD.getIsoCode())))
                    .andExpect(jsonPath("$.currencies[9].usedInCountries", hasSize(CurrencyCode.USD.getCountryCodesList().size())))
                    .andExpect(jsonPath("$.currencies[9].usedInCountries[0].code", is(CountryCode.AS.getCode())))
                    .andExpect(jsonPath("$.currencies[9].usedInCountries[0].name", is(CountryCode.AS.getName())));
        }

    }

}