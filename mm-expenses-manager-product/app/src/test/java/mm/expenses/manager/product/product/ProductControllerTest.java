package mm.expenses.manager.product.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.message.ProductManagementMessage;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.web.exception.ExceptionMessage;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.product.ProductApplicationTest;
import mm.expenses.manager.product.api.product.model.SortProductRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static mm.expenses.manager.product.product.ProductHelper.*;
import static mm.expenses.manager.product.product.ProductWebApi.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ProductControllerTest extends ProductApplicationTest {

    @Autowired
    private ProductSpecificationHandler specificationHandler;

    @Captor
    private ArgumentCaptor<ProductManagementMessage> productMessageArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<CurrencyConversionRequest>> currencyConversionRequestCaptor;

    @Nested
    class FindAll {

        @Test
        void shouldFindAll() throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, CurrencyCode.EUR);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @EnumSource(TextOperationRequest.class)
        void shouldFindAll_byGeneralQueryParameter(final TextOperationRequest operation) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, CurrencyCode.EUR);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, String.format("name:%s=%s", operation, PRODUCT_NAME)).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @Test
        void shouldFindAll_onlyDeleted() throws Exception {
            // given
            final var expectedProductDeleted = createProductDeleted();

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProductDeleted)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.IS_DELETED_PROPERTY, Boolean.TRUE.toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProductDeleted.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProductDeleted.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProductDeleted.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProductDeleted.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProductDeleted.getDetails())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyDifferentThanDefault.class)
        void shouldFindAll_shouldConvertCurrencyFlag(final CurrencyCode currencyCode) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, currencyCode);
            final var expectedConvertedCurrencyResponse = createCurrencyConversionResponse(expectedProduct);
            val convertedCurrenciesResponse = List.of(expectedConvertedCurrencyResponse);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));
            Mockito.when(financeApiClient.convertMultipleRates(currencyConversionRequestCaptor.capture())).thenReturn(convertedCurrenciesResponse);

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, Boolean.TRUE.toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));

            val convertedCurrenciesRequestList = currencyConversionRequestCaptor.getValue();
            verify(financeApiClient).convertMultipleRates(convertedCurrenciesRequestList);
            assertThat(convertedCurrenciesRequestList).isNotNull()
                    .isNotEmpty()
                    .hasSize(1);

            val convertedCurrencyRequest = convertedCurrenciesRequestList.get(0);
            assertThat(convertedCurrencyRequest.getId()).isEqualTo(expectedProduct.getId().toString());
            assertThat(convertedCurrencyRequest.getDate()).isEqualTo(expectedConvertedCurrencyResponse.getDate());
            assertThat(convertedCurrencyRequest.getFrom().getCode()).isEqualTo(currencyCode.getCode());
            assertThat(convertedCurrencyRequest.getFrom().getValue()).isEqualTo(expectedProduct.getPrice().getValue().doubleValue());
            assertThat(convertedCurrencyRequest.getTo().getCode()).isEqualTo(DEFAULT_CURRENCY.getCode());

            verify(financeApiClient).convertMultipleRates(convertedCurrenciesRequestList);
        }

        @Test
        void shouldFindAll_shouldConvertCurrencyFlagButConversionIsNotNeeded() throws Exception {
            // given
            final var expectedProduct = createProduct();

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, Boolean.TRUE.toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));

            verifyNoInteractions(financeApiClient);
        }

        @ParameterizedTest
        @EnumSource(TextOperationRequest.class)
        void shouldFindAll_byName(final TextOperationRequest operation) throws Exception {
            // given
            final var expectedProduct = createProduct();

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.NAME_PROPERTY, PRODUCT_NAME).param(ProductFilter.NAME_OPERATION_PROPERTY, operation.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @EnumSource(PriceValueArguments.class)
        void shouldFindAll_byPriceValue(final PriceValueArguments operation) throws Exception {
            // given
            final var expectedProduct = createProduct();

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.PRICE_VALUE_PROPERTY, expectedProduct.getPrice().getValue().toString()).param(ProductFilter.PRICE_VALUE_OPERATION_PROPERTY, operation.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @ArgumentsSource(PriceCurrencyArguments.class)
        void shouldFindAll_byPriceCurrency(final TextOperationRequest operation, final CurrencyCode currencyCode) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, currencyCode);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.PRICE_CURRENCY_PROPERTY, expectedProduct.getPrice().getCurrency().toString()).param(ProductFilter.PRICE_CURRENCY_OPERATION_PROPERTY, operation.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @ArgumentsSource(FullPriceArguments.class)
        void shouldFindAll_byPriceValueAndCurrency(final TextOperationRequest textOperation, final PriceValueArguments numberOperation, final CurrencyCode currencyCode) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, currencyCode);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.PRICE_VALUE_PROPERTY, expectedProduct.getPrice().getValue().toString()).param(ProductFilter.PRICE_VALUE_OPERATION_PROPERTY, numberOperation.name())
                                    .param(ProductFilter.PRICE_CURRENCY_PROPERTY, expectedProduct.getPrice().getCurrency().toString()).param(ProductFilter.PRICE_CURRENCY_OPERATION_PROPERTY, textOperation.name())
                                    .contentType(DATA_FORMAT_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @ArgumentsSource(ProductNameAndPriceValueArguments.class)
        void shouldFindAll_byNameAndPriceValue(final TextOperationRequest nameTextOperation, final PriceValueArguments priceValueNumberOperation) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, CurrencyCode.PLN);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.NAME_PROPERTY, PRODUCT_NAME).param(ProductFilter.NAME_OPERATION_PROPERTY, nameTextOperation.name())
                                    .param(ProductFilter.PRICE_VALUE_PROPERTY, expectedProduct.getPrice().getValue().toString()).param(ProductFilter.PRICE_VALUE_OPERATION_PROPERTY, priceValueNumberOperation.name())
                                    .contentType(DATA_FORMAT_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @ArgumentsSource(ProductNameAndPriceCurrencyArguments.class)
        void shouldFindAll_byNameAndPriceCurrency(final TextOperationRequest nameTextOperation, final TextOperationRequest priceCurrencyTextOperation, final CurrencyCode currencyCode) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, currencyCode);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.NAME_PROPERTY, PRODUCT_NAME).param(ProductFilter.NAME_OPERATION_PROPERTY, nameTextOperation.name())
                                    .param(ProductFilter.PRICE_CURRENCY_PROPERTY, expectedProduct.getPrice().getCurrency().toString()).param(ProductFilter.PRICE_CURRENCY_OPERATION_PROPERTY, priceCurrencyTextOperation.name())
                                    .contentType(DATA_FORMAT_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @ParameterizedTest
        @ArgumentsSource(ProductNameAndFullPriceArguments.class)
        void shouldFindAll_byNameAndPriceValueAndCurrency(final TextOperationRequest nameTextOperation, final TextOperationRequest priceCurrencyTextOperation, final PriceValueArguments priceValueNumberOperation, final CurrencyCode currencyCode) throws Exception {
            // given
            final var expectedProduct = createProduct(PRODUCT_NAME, currencyCode);

            // when
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedProduct)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.NAME_PROPERTY, PRODUCT_NAME).param(ProductFilter.NAME_OPERATION_PROPERTY, nameTextOperation.name())
                                    .param(ProductFilter.PRICE_VALUE_PROPERTY, expectedProduct.getPrice().getValue().toString()).param(ProductFilter.PRICE_VALUE_OPERATION_PROPERTY, priceValueNumberOperation.name())
                                    .param(ProductFilter.PRICE_CURRENCY_PROPERTY, expectedProduct.getPrice().getCurrency().toString()).param(ProductFilter.PRICE_CURRENCY_OPERATION_PROPERTY, priceCurrencyTextOperation.name())
                                    .contentType(DATA_FORMAT_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedProduct.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(expectedProduct.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(expectedProduct.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(expectedProduct.getDetails())));
        }

        @Test
        void shouldSortByNameAsc() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.PLN);
            final var product_2 = createProduct("p2", CurrencyCode.PLN);

            // when
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(PaginationConfig.SORT, SortProductRequest.NAME_ASC.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId().toString())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))

                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldSortByNameDesc() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.PLN);
            final var product_2 = createProduct("p2", CurrencyCode.PLN);

            // when
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_2, product_1)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(PaginationConfig.SORT, SortProductRequest.NAME_DESC.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_2.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(product_2.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_2.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(product_2.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_1.getId().toString())))
                    .andExpect(jsonPath("$.content[1].name", is(product_1.getName())))

                    .andExpect(jsonPath("$.content[1].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_1.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[1].details", is(product_1.getDetails())));
        }

        @Test
        void shouldSortByPriceValueAsc() throws Exception {
            // given
            final var product_1 = createProduct("p1", BigDecimal.valueOf(2.31), CurrencyCode.PLN);
            final var product_2 = createProduct("p2", BigDecimal.valueOf(4.35), CurrencyCode.PLN);

            // when
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(PaginationConfig.SORT, SortProductRequest.PRICE_ASC.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId().toString())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))

                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldSortByPriceValueDesc() throws Exception {
            // given
            final var product_1 = createProduct("p1", BigDecimal.valueOf(3.17), CurrencyCode.PLN);
            final var product_2 = createProduct("p2", BigDecimal.valueOf(5.12), CurrencyCode.PLN);

            // when
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_2, product_1)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(PaginationConfig.SORT, SortProductRequest.PRICE_DESC.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_2.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(product_2.getName())))

                    .andExpect(jsonPath("$.content[0].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_2.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].details", is(product_2.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_1.getId().toString())))
                    .andExpect(jsonPath("$.content[1].name", is(product_1.getName())))

                    .andExpect(jsonPath("$.content[1].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_1.getPrice().getCurrency().toString())))

                    .andExpect(jsonPath("$.content[1].details", is(product_1.getDetails())));
        }

    }


    @Nested
    class FindAll_ErrorCodes {

        @Test
        void shouldReturnBadRequest_whenNameIsPassedButNameOperationIsMissing() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.NAME_PROPERTY, PRODUCT_NAME))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRODUCT_NAME_MISSING_OPERATOR.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRODUCT_NAME_MISSING_OPERATOR.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsPassedButPriceValueIsMissing() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.PRICE_VALUE_PROPERTY, "2.31"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_VALUE_MISSING_OPERATOR.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_VALUE_MISSING_OPERATOR.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsPassedButPriceCurrencyIsMissing() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.PRICE_CURRENCY_PROPERTY, "PLN"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_CURRENCY_MISSING_OPERATOR.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_CURRENCY_MISSING_OPERATOR.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenGeneralQueryPassedWithSpecificNameParameter() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, "name:equal=test").param(ProductFilter.NAME_PROPERTY, "test2"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(EntityFilter.FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE)))
                    .andExpect(jsonPath("$.message", is(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenGeneralQueryPassedWithSpecificNameOperatorParameter() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, "name:equal=test").param(ProductFilter.NAME_OPERATION_PROPERTY, "equal"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(EntityFilter.FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE)))
                    .andExpect(jsonPath("$.message", is(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenGeneralQueryPassedWithSpecificPriceValueParameter() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, "price.value:equal=1.21").param(ProductFilter.PRICE_VALUE_PROPERTY, "3.55"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(EntityFilter.FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE)))
                    .andExpect(jsonPath("$.message", is(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenGeneralQueryPassedWithSpecificPriceValueOperatorParameter() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, "price.value:equal=2.11").param(ProductFilter.PRICE_VALUE_OPERATION_PROPERTY, "equal"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(EntityFilter.FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE)))
                    .andExpect(jsonPath("$.message", is(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenGeneralQueryPassedWithSpecificPriceCurrencyParameter() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, "price.currency:equal=PLN").param(ProductFilter.PRICE_CURRENCY_PROPERTY, "EUR"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(EntityFilter.FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE)))
                    .andExpect(jsonPath("$.message", is(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenGeneralQueryPassedWithSpecificPriceCurrencyOperatorParameter() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(ProductFilter.GENERAL_QUERY_PROPERTY, "price.currency:equal=USD").param(ProductFilter.PRICE_CURRENCY_OPERATION_PROPERTY, "equal"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(EntityFilter.FILTERING_BY_EXPLICIT_QUERY_ONLY_ERROR_CODE)))
                    .andExpect(jsonPath("$.message", is(SpecificationCriteriaException.FILTERING_BY_EXPLICIT_QUERY_ONLY)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

    }


    @Nested
    class CreateProduct {

        @Test
        void shouldCreateNewProduct() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.EUR);
            final var expectedResult = createProductFromProductRequest(request);

            // when
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expectedResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.id", is(expectedResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedResult.getName())))
                    .andExpect(jsonPath("$.price.value", is(expectedResult.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(expectedResult.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.details", is(expectedResult.getDetails())));

            verify(asyncProducer, times(1)).send(productMessageArgumentCaptor.capture());
            final var event = productMessageArgumentCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getId()).isEqualTo(expectedResult.getId());
            assertThat(event.getName()).isEqualTo(expectedResult.getName());
            assertThat(event.getPrice().getValue().doubleValue()).isEqualTo(expectedResult.getPrice().getValue().doubleValue());
            assertThat(event.getPrice().getCurrency().toString()).isEqualTo(expectedResult.getPrice().getCurrency().toString());
            assertThat(event.getDetails()).isEqualTo(expectedResult.getDetails());
            assertThat(event.getOperation()).isEqualTo(AsyncKafkaOperation.CREATE);
        }

        @Test
        void shouldReturnBadRequest_whenEmptyNameIsPassed() throws Exception {
            // given
            final var emptyName = "";

            final var request = createProductRequest(emptyName, CurrencyCode.CAD);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenDetailsAsNullIsPassed() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.JPY, null);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsZero() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, BigDecimal.ZERO, CurrencyCode.AUD);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsNull() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, null, CurrencyCode.JPY);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsNull() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, null);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUnknown() throws Exception {
            // given
            final var request = createProductRequestWithUnknownCurrency(PRODUCT_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUndefined() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.UNDEFINED);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(asyncProducer);
        }

    }


    @Nested
    class UpdateProduct {

        @Test
        void shouldReturnBadRequest_whenEmptyNameIsPassed() throws Exception {
            // given
            final var emptyName = "";
            final var existed = createProduct();
            final var request = updateProductRequest(emptyName);

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsZero() throws Exception {
            // given
            final var existed = createProduct();
            final var request = updateProductRequest(BigDecimal.ZERO, false);

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUndefined() throws Exception {
            // given
            final var existed = createProduct();
            final var request = updateProductRequest(CurrencyCode.UNDEFINED, false);

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            // given
            final var request = createSimpleProduct(PRODUCT_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldReturnConflict_whenNoDataToUpdateWasPassed() throws Exception {
            // given
            final var existed = createProduct();
            var request = new UpdateProductRequest();

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldUpdateProduct() throws Exception {
            // given
            final var newName = "new name";
            final var newPriceValue = BigDecimal.valueOf(5d);
            final var newPriceCurrency = CurrencyCode.EUR;
            final Map<String, Object> newDetails = Map.of("key1", "value1", "key2", 2);

            final var existed = createProduct();
            final var request = updateProductRequest(newName, newPriceValue, newPriceCurrency, newDetails);
            final var expected = createProductFromUpdateProductRequest(request);

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expected);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID.toString())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(newName)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.value", Matchers.is(newPriceValue.doubleValue())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.currency", Matchers.is(newPriceCurrency.getCode())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.details", Matchers.is(newDetails)));

            verify(asyncProducer, times(1)).send(productMessageArgumentCaptor.capture());
            final var event = productMessageArgumentCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getId()).isEqualTo(expected.getId());
            assertThat(event.getName()).isEqualTo(expected.getName());
            assertThat(event.getPrice().getValue().doubleValue()).isEqualTo(expected.getPrice().getValue().doubleValue());
            assertThat(event.getPrice().getCurrency().toString()).isEqualTo(expected.getPrice().getCurrency().toString());
            assertThat(event.getDetails()).isEqualTo(expected.getDetails());
            assertThat(event.getOperation()).isEqualTo(AsyncKafkaOperation.UPDATE);
        }

        @Test
        void shouldUpdateProduct_whenOnlyNameIsPassed() throws Exception {
            // given
            final var newName = "new name";

            final var existed = createProduct();
            final var request = createSimpleProduct(newName);
            final var expected = existed.toBuilder().name(newName).build();

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expected);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID.toString())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(newName)))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));

            verify(asyncProducer, times(1)).send(productMessageArgumentCaptor.capture());
            final var event = productMessageArgumentCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getId()).isEqualTo(expected.getId());
            assertThat(event.getName()).isEqualTo(expected.getName());
            assertThat(event.getPrice().getValue().doubleValue()).isEqualTo(expected.getPrice().getValue().doubleValue());
            assertThat(event.getPrice().getCurrency().toString()).isEqualTo(expected.getPrice().getCurrency().toString());
            assertThat(event.getDetails()).isEqualTo(expected.getDetails());
            assertThat(event.getOperation()).isEqualTo(AsyncKafkaOperation.UPDATE);
        }

        @Test
        void shouldUpdateProduct_whenOnlyPriceValueIsPassed() throws Exception {
            // given
            final var newPriceValue = BigDecimal.valueOf(5d);

            final var existed = createProduct();
            final var request = updatePriceRequest(newPriceValue);
            final var expected = existed.toBuilder().price(existed.getPrice().toBuilder().value(newPriceValue).build()).build();

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expected);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID.toString())))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.value", Matchers.is(newPriceValue.doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));

            verify(asyncProducer, times(1)).send(productMessageArgumentCaptor.capture());
            final var event = productMessageArgumentCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getId()).isEqualTo(expected.getId());
            assertThat(event.getName()).isEqualTo(expected.getName());
            assertThat(event.getPrice().getValue().doubleValue()).isEqualTo(expected.getPrice().getValue().doubleValue());
            assertThat(event.getPrice().getCurrency().toString()).isEqualTo(expected.getPrice().getCurrency().toString());
            assertThat(event.getDetails()).isEqualTo(expected.getDetails());
            assertThat(event.getOperation()).isEqualTo(AsyncKafkaOperation.UPDATE);
        }

        @Test
        void shouldUpdateProduct_whenOnlyPriceCurrencyIsPassed() throws Exception {
            // given
            final var newPriceCurrency = CurrencyCode.USD;

            final var existed = createProduct();
            final var request = updatePriceRequest(newPriceCurrency);
            final var expected = existed.toBuilder().price(existed.getPrice().toBuilder().currency(newPriceCurrency).build()).build();

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expected);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID.toString())))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.currency", Matchers.is(newPriceCurrency.getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));

            verify(asyncProducer, times(1)).send(productMessageArgumentCaptor.capture());
            final var event = productMessageArgumentCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getId()).isEqualTo(expected.getId());
            assertThat(event.getName()).isEqualTo(expected.getName());
            assertThat(event.getPrice().getValue().doubleValue()).isEqualTo(expected.getPrice().getValue().doubleValue());
            assertThat(event.getPrice().getCurrency().toString()).isEqualTo(expected.getPrice().getCurrency().toString());
            assertThat(event.getDetails()).isEqualTo(expected.getDetails());
            assertThat(event.getOperation()).isEqualTo(AsyncKafkaOperation.UPDATE);
        }

    }


    @Nested
    class DeleteProduct {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + ID))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            verifyNoInteractions(asyncProducer);
        }

        @Test
        void shouldDeleteProduct() throws Exception {
            // given
            final var existed = createProduct();
            final var expected = existed.toBuilder().isDeleted(true).build();

            // when
            Mockito.when(productRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), anyBoolean())).thenReturn(Optional.of(existed));
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expected);

            // then
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + ID)).andExpect(MockMvcResultMatchers.status().isNoContent());

            verify(asyncProducer, times(1)).send(productMessageArgumentCaptor.capture());
            final var event = productMessageArgumentCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getId()).isEqualTo(expected.getId());
            assertThat(event.getName()).isEqualTo(expected.getName());
            assertThat(event.getPrice().getValue().doubleValue()).isEqualTo(expected.getPrice().getValue().doubleValue());
            assertThat(event.getPrice().getCurrency().toString()).isEqualTo(expected.getPrice().getCurrency().toString());
            assertThat(event.getDetails()).isEqualTo(expected.getDetails());
            assertThat(event.getOperation()).isEqualTo(AsyncKafkaOperation.DELETE);
        }

    }


    @Nested
    class FindProductById {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + ID))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldFindProductById_defaultIsDeletedFalse() throws Exception {
            // given
            final var existed = createProduct();

            // when
            Mockito.when(productRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(jsonPath("$.id", is(existed.getId().toString())))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
        }

        @Test
        void shouldNotFindProductById_isDeletedTrue() throws Exception {
            // given
            final var existed = createProductDeleted();

            // when
            Mockito.when(productRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.empty());

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

    }

    @Getter
    @RequiredArgsConstructor
    public enum TextOperationRequest {

        equal("equal"),

        notEqual("notEqual"),

        startsWith("startsWith"),

        endsWith("endsWith"),

        contains("contains");

        private final String value;

    }

    @Getter
    @RequiredArgsConstructor
    public enum PriceValueArguments {

        equal("equal"),

        notEqual("notEqual"),

        lessThan("lessThan"),

        lessThanOrEqual("lessThanOrEqual"),

        greaterThan("greaterThan"),

        greaterThanOrEqual("greaterThanOrEqual");

        private final String value;

    }

    public static class CurrencyDifferentThanDefault implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val currencies = Arrays.asList(CurrencyCode.values());
            return currencies.stream()
                    .filter(currencyCode -> !DEFAULT_CURRENCY.equals(currencyCode))
                    .map(Arguments::of);
        }

    }

    public static class PriceCurrencyArguments implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val textOperations = Arrays.asList(TextOperationRequest.values());
            val currencies = Arrays.asList(CurrencyCode.values());
            val result = new ArrayList<Arguments>();

            textOperations.forEach(textOperation -> {
                currencies.forEach(currencyCode -> {
                    result.add(Arguments.of(textOperation, currencyCode));
                });
            });
            return result.stream();
        }

    }

    public static class FullPriceArguments implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val textOperations = Arrays.asList(TextOperationRequest.values());
            val numberOperations = Arrays.asList(PriceValueArguments.values());
            val currencies = Arrays.asList(CurrencyCode.values());
            val result = new ArrayList<Arguments>();

            textOperations.forEach(textOperation -> {
                numberOperations.forEach(numberOperation -> {
                    currencies.forEach(currencyCode -> {
                        result.add(Arguments.of(textOperation, numberOperation, currencyCode));
                    });
                });
            });
            return result.stream();
        }

    }

    public static class ProductNameAndPriceValueArguments implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val nameTextOperations = Arrays.asList(TextOperationRequest.values());
            val numberOperations = Arrays.asList(PriceValueArguments.values());
            val result = new ArrayList<Arguments>();

            nameTextOperations.forEach(nameTextOperation -> {
                numberOperations.forEach(numberOperation -> {

                    result.add(Arguments.of(nameTextOperation, numberOperation));
                });
            });
            return result.stream();
        }

    }

    public static class ProductNameAndPriceCurrencyArguments implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val nameTextOperations = Arrays.asList(TextOperationRequest.values());
            val textOperations = Arrays.asList(TextOperationRequest.values());
            val currencies = Arrays.asList(CurrencyCode.values());
            val result = new ArrayList<Arguments>();

            nameTextOperations.forEach(nameTextOperation -> {
                textOperations.forEach(textOperation -> {
                    currencies.forEach(currencyCode -> {
                        result.add(Arguments.of(nameTextOperation, textOperation, currencyCode));
                    });
                });
            });
            return result.stream();
        }

    }

    public static class ProductNameAndFullPriceArguments implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val nameTextOperations = Arrays.asList(TextOperationRequest.values());
            val textOperations = Arrays.asList(TextOperationRequest.values());
            val numberOperations = Arrays.asList(PriceValueArguments.values());
            val currencies = Arrays.asList(CurrencyCode.values());
            val result = new ArrayList<Arguments>();

            nameTextOperations.forEach(nameTextOperation -> {
                textOperations.forEach(textOperation -> {
                    numberOperations.forEach(numberOperation -> {
                        currencies.forEach(currencyCode -> {
                            result.add(Arguments.of(nameTextOperation, textOperation, numberOperation, currencyCode));
                        });
                    });
                });
            });
            return result.stream();
        }

    }

}