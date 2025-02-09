package mm.expenses.manager.order.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.order.OrderApplicationTest;
import mm.expenses.manager.order.api.order.model.OrderIds;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;
import java.util.stream.Stream;

import static mm.expenses.manager.order.order.OrderHelper.*;
import static mm.expenses.manager.order.order.OrderWebApi.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class OrderControllerTest extends OrderApplicationTest {

    @Autowired
    private OrderSpecificationHandler specificationHandler;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<List<Order>> ordersCaptor;

    @Captor
    private ArgumentCaptor<List<CurrencyConversionRequest>> currencyConversionRequestCaptor;

    @Nested
    class FindAll {

        @Test
        void shouldFindAll() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

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

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @Test
        void shouldFindAll_onlyDeleted() throws Exception {
            // given
            final var expectedProductDeleted = OrderHelper.createProduct(true);
            final var requestDeleted = OrderHelper.createOrderRequest(ORDER_NAME, expectedProductDeleted);
            final var expectedOrderResultDeleted = OrderHelper.createOrderFromOrderRequest(requestDeleted, expectedProductDeleted);

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResultDeleted)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderFilter.IS_DELETED_PROPERTY, Boolean.TRUE.toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResultDeleted.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResultDeleted.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResultDeleted.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResultDeleted.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProductDeleted.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResultDeleted.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResultDeleted.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResultDeleted.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyDifferentThanDefault.class)
        void shouldFindAll_shouldConvertCurrencyFlag(final CurrencyCode currencyCode) throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct(currencyCode);
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var expectedConvertedCurrencyResponse = OrderHelper.createCurrencyConversionResponse(expectedProduct);
            val convertedCurrenciesResponse = List.of(expectedConvertedCurrencyResponse);

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));
            Mockito.when(financeApiClient.convertMultipleRates(currencyConversionRequestCaptor.capture())).thenReturn(convertedCurrenciesResponse);

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, Boolean.TRUE.toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].price", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].price[0].amount").value(expectedConvertedCurrencyResponse.getTo().getValue().doubleValue()))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].price[0].currency", is(expectedConvertedCurrencyResponse.getTo().getCode())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            val convertedCurrenciesRequestList = currencyConversionRequestCaptor.getValue();
            verify(financeApiClient).convertMultipleRates(convertedCurrenciesRequestList);
            assertThat(convertedCurrenciesRequestList).isNotNull()
                    .isNotEmpty()
                    .hasSize(1);

            val convertedCurrencyRequest = convertedCurrenciesRequestList.get(0);
            assertThat(convertedCurrencyRequest.getId()).isEqualTo(expectedProduct.getId().toString());
            assertThat(convertedCurrencyRequest.getDate()).isEqualTo(expectedConvertedCurrencyResponse.getDate());
            assertThat(convertedCurrencyRequest.getFrom().getCode()).isEqualTo(expectedProduct.getPrice().get(0).getCurrency().getCode());
            assertThat(convertedCurrencyRequest.getFrom().getValue()).isEqualTo(expectedProduct.getPrice().get(0).getValue().doubleValue());
            assertThat(convertedCurrencyRequest.getTo().getCode()).isEqualTo(DEFAULT_CURRENCY.getCode());
        }

        @Test
        void shouldFindAll_shouldConvertCurrencyFlagButConversionIsNotNeeded() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, Boolean.TRUE.toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @ParameterizedTest
        @EnumSource(TextOperationRequest.class)
        void shouldFindAll_byName(final TextOperationRequest operation) throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderFilter.NAME_PROPERTY, ORDER_NAME).param(OrderFilter.NAME_OPERATION_PROPERTY, operation.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @ParameterizedTest
        @EnumSource(NumberOperationRequest.class)
        void shouldFindAll_byProductsCount(final NumberOperationRequest operation) throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderFilter.PRODUCTS_COUNT_PROPERTY, String.valueOf(productsCount)).param(OrderFilter.PRODUCTS_COUNT_OPERATION_PROPERTY, operation.name()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(TextAndNumberOperations.class)
        void shouldFindAll_byNameAndProductsCount(final TextOperationRequest textOperation, final NumberOperationRequest numberOperationRequest) throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();

            // when
            when(orderRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderFilter.NAME_PROPERTY, String.valueOf(productsCount)).param(OrderFilter.NAME_OPERATION_PROPERTY, textOperation.name())
                            .param(OrderFilter.PRODUCTS_COUNT_PROPERTY, String.valueOf(productsCount)).param(OrderFilter.PRODUCTS_COUNT_OPERATION_PROPERTY, numberOperationRequest.name())
                            .contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.content[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

    }

    @Nested
    class FindOrderById {

        @Test
        void shouldFindOrderByIdAndDefaultNotDeleted() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @Test
        void shouldFindOrderByIdAndIsDeleted() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            expectedOrderResult.setDeleted(true);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.eq(true))).thenReturn(Optional.of(expectedOrderResult));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + expectedOrderResult.getId().toString()).param(OrderFilter.IS_DELETED_PROPERTY, String.valueOf(true)).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @Test
        void shouldFindOrderByIdAndIsNotDeleted() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            expectedOrderResult.setDeleted(false);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.eq(false))).thenReturn(Optional.of(expectedOrderResult));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + expectedOrderResult.getId().toString()).param(OrderFilter.IS_DELETED_PROPERTY, String.valueOf(false)).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));
        }

        @Test
        void shouldNotFindOrderById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindOrderById_shouldConvertProductAndOrderCurrencyToDefaultCurrency(final CurrencyCode currency) throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct(currency);
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var expectedConvertedCurrencyResponse = OrderHelper.createCurrencyConversionResponse(expectedProduct);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(financeApiClient.convertMultipleRates(currencyConversionRequestCaptor.capture())).thenReturn(List.of(expectedConvertedCurrencyResponse));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + expectedOrderResult.getId().toString()).param(OrderFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, String.valueOf(true)).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount").value(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue()))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].price", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].price[0].amount").value(expectedConvertedCurrencyResponse.getTo().getValue().doubleValue()))
                    .andExpect(jsonPath("$.orderedProducts[0].price[0].currency", is(expectedConvertedCurrencyResponse.getTo().getCode())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount").value(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue()))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            val convertedCurrenciesRequestList = currencyConversionRequestCaptor.getValue();
            assertThat(convertedCurrenciesRequestList).isNotNull()
                    .isNotEmpty()
                    .hasSize(1);

            val convertedCurrencyRequest = convertedCurrenciesRequestList.get(0);
            assertThat(convertedCurrencyRequest.getId()).isEqualTo(expectedProduct.getId().toString());
            assertThat(convertedCurrencyRequest.getDate()).isEqualTo(expectedConvertedCurrencyResponse.getDate());
            assertThat(convertedCurrencyRequest.getFrom().getCode()).isEqualTo(expectedProduct.getPrice().get(0).getCurrency().getCode());
            assertThat(convertedCurrencyRequest.getFrom().getValue()).isEqualTo(expectedProduct.getPrice().get(0).getValue().doubleValue());
            assertThat(convertedCurrencyRequest.getTo().getCode()).isEqualTo(DEFAULT_CURRENCY.getCode());
        }

    }


    @Nested
    class DeleteOrderById {

        @Test
        void shouldDeleteOrderById() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            final var deletedOrderResult = expectedOrderResult.toBuilder().isDeleted(true).build();

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(deletedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            verify(orderRepository, times(1)).save(orderCaptor.capture());
            final var deletedOrder = orderCaptor.getValue();
            assertThat(deletedOrder).isNotNull();
            assertThat(deletedOrder.getId()).isEqualTo(deletedOrderResult.getId());
            assertThat(deletedOrder.getName()).isEqualTo(expectedOrderResult.getName());
            assertThat(deletedOrder.getProducts()).containsExactlyInAnyOrderElementsOf(expectedOrderResult.getProducts());
            assertThat(deletedOrder.isDeleted()).isTrue();
        }

        @Test
        void shouldNotDeleteOrderById_whenOrderIsAlreadyDeleted() throws Exception {
            // given & when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.eq(false))).thenReturn(Optional.empty());

            // then
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

        @Test
        void shouldNotDeleteOrderById_whenOrderDoesNotExists() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

    }


    @Nested
    class DeleteOrdersByIds {

        @Test
        void shouldDeleteOrdersByIds() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request_1 = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var request_2 = OrderHelper.createOrderRequest("Another order", expectedProduct);
            final var expectedOrderResult_1 = OrderHelper.createOrderFromOrderRequest(request_1, expectedProduct);
            final var expectedOrderResult_2 = OrderHelper.createOrderFromOrderRequest(UUID.randomUUID(), request_2, expectedProduct);

            final var deletedOrderResult_1 = expectedOrderResult_1.toBuilder().isDeleted(true).build();
            final var deletedOrderResult_2 = expectedOrderResult_2.toBuilder().isDeleted(true).build();

            final var orderIds = new OrderIds();
            orderIds.addIdsItem(expectedOrderResult_1.getId());
            orderIds.addIdsItem(expectedOrderResult_2.getId());

            // when
            Mockito.when(orderRepository.findAllByIdInAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(List.of(expectedOrderResult_1, expectedOrderResult_2));
            Mockito.when(orderRepository.saveAll(ArgumentMatchers.any())).thenReturn(List.of(deletedOrderResult_1, deletedOrderResult_2));

            // then
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/remove").contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(orderIds)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            verify(orderRepository, times(1)).saveAll(ordersCaptor.capture());
            final var deletedOrders = orderCaptor.getAllValues();
            deletedOrders.forEach(deletedOrder -> {
                var orderToCompare = Stream.of(expectedOrderResult_1, expectedOrderResult_2).filter(order -> order.getId().equals(deletedOrder.getId())).findAny().orElseThrow();

                assertThat(deletedOrder).isNotNull();
                assertThat(deletedOrder.getId()).isEqualTo(orderToCompare.getId());
                assertThat(deletedOrder.getName()).isEqualTo(orderToCompare.getName());
                assertThat(deletedOrder.getProducts()).containsExactlyInAnyOrderElementsOf(orderToCompare.getProducts());
                assertThat(deletedOrder.isDeleted()).isTrue();
            });
        }

        @Test
        void shouldNotDeleteOrdersByIds_whenOneOrdersDoesNotExists() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request_1 = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var request_2 = OrderHelper.createOrderRequest("Another order", expectedProduct);
            final var expectedOrderResult_1 = OrderHelper.createOrderFromOrderRequest(request_1, expectedProduct);
            final var expectedOrderResult_2 = OrderHelper.createOrderFromOrderRequest(UUID.randomUUID(), request_2, expectedProduct);
            expectedOrderResult_2.setDeleted(true);

            final var orderIds = new OrderIds();
            orderIds.addIdsItem(expectedOrderResult_1.getId());
            orderIds.addIdsItem(expectedOrderResult_2.getId());

            // when
            Mockito.when(orderRepository.findAllByIdInAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(List.of(expectedOrderResult_1));

            // then
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/remove").contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(orderIds)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

        @Test
        void shouldNotDeleteOrdersByIds_whenOrdersDoNotExist() throws Exception {
            final var orderIds = new OrderIds();
            orderIds.addIdsItem(UUID.randomUUID());
            orderIds.addIdsItem(UUID.randomUUID());

            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/remove").contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(orderIds)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

    }


    @Nested
    class CreateOrder {

        @Test
        void shouldCreateNewOrder() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var expectedConvertedCurrencyResponse = OrderHelper.createCurrencyConversionResponse(expectedProduct);

            // when
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProduct));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount").value(expectedOrderResult.getPriceSummary().get(0).getValue().doubleValue()))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].price", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].price[0].amount").value(expectedConvertedCurrencyResponse.getTo().getValue()))
                    .andExpect(jsonPath("$.orderedProducts[0].price[0].currency", is(expectedConvertedCurrencyResponse.getTo().getCode())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount").value(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue()))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenNullNameIsPassed() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(null, expectedProduct);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenNullProductsArePassed() throws Exception {
            // given
            final var request = OrderHelper.createOrderRequest(null, ORDER_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenEmptyProductsArePassed() throws Exception {
            // given
            final var request = OrderHelper.createOrderRequest(List.of(), ORDER_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenUnknownProductIsPassed() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenNoIdForProductIsPassed() throws Exception {
            // given
            final var request = OrderHelper.createOrderRequestEmptyProductId(ORDER_NAME);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenNoQuantityForProductIsPassed() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequestSkipQuantity(ORDER_NAME, expectedProduct, true);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenQuantityForProductIsLessThan0() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct, -0.5);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldReturnBadRequest_whenQuantityForProductIsEqualTo0() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct, 0.0);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

    }


    @Nested
    class UpdateOrder {

        @Test
        void shouldNotUpdateOrder_whenProductQuantityIsEqualToZero() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var expectedProductToAdd = OrderHelper.createProduct();
            final var updateRequest = OrderHelper.updateOrderRequest("test", expectedProduct, 0.0, expectedProductToAdd);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, expectedProduct, expectedProductToAdd);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProductToAdd));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldNotUpdateOrder_whenProductQuantityIsEqualLessThanZero() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var expectedProductToAdd = OrderHelper.createProduct();
            final var updateRequest = OrderHelper.updateOrderRequest("test", expectedProduct, -1.5, expectedProductToAdd);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, expectedProduct, expectedProductToAdd);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProductToAdd));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldNotUpdateOrder_whenOrderDoesNotExists() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var expectedProductToAdd = OrderHelper.createProduct();
            final var updateRequest = OrderHelper.updateOrderRequest("test", expectedProduct, -1.5, expectedProductToAdd);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.empty());

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldNotUpdateOrder_whenNoChanges() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var updateRequest = OrderHelper.updateOrderRequestEmpty();

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var expectedProductToAdd = OrderHelper.createProduct();
            final var updateRequest = OrderHelper.updateOrderRequest("test", expectedProduct, 3.0, expectedProductToAdd);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, expectedProduct, expectedProductToAdd);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProductToAdd));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(2)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts[1].id", is(expectedProductToAdd.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[1].quantity", is(expectedUpdatedOrderResult.getProducts().get(1).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenProductIsRemoved() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var updateRequest = OrderHelper.updateOrderRequest(List.of(expectedProduct.getId()));
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, newOrderRequest);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(0)))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(0)));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenNewProductIsAddedAsExistingProductAndRemoveUpdatedProduct() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var expectedProductToAdd = OrderHelper.createProduct();
            final var updateRequest = OrderHelper.updateOrderRequest("test", expectedProduct, 3.2, expectedProductToAdd, true);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, newOrderRequest, expectedProduct, expectedProductToAdd, true, true);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProductToAdd));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProductToAdd.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenCurrentProductIsUpdated() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var updateRequest = OrderHelper.updateOrderRequest("test", expectedProduct, 3.5, null);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, expectedProduct, null);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenNewProductIsAddedAsExistingProductToUpdate() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, null);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var updateRequest = OrderHelper.updateOrderRequest(null, expectedProduct, 3.5, null);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, newOrderRequest, expectedProduct, null, false);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProduct));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenNewProductIsAdded() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var expectedProductToAdd = OrderHelper.createProduct();
            final var updateRequest = OrderHelper.updateOrderRequest("test", null, 3.2, expectedProductToAdd);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, newOrderRequest, expectedProduct, expectedProductToAdd);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProductToAdd));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(2)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts[1].id", is(expectedProductToAdd.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[1].quantity", is(expectedUpdatedOrderResult.getProducts().get(1).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenNewNameIsAdded() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var updateRequest = OrderHelper.updateOrderRequest("test name", expectedProduct, 3.0, null);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, expectedProduct, null);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedUpdatedOrderResult.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
        }

        @Test
        void shouldUpdateOrder_whenNameIsNotPassedAndOldNameIsKept() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var newOrderRequest = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(newOrderRequest, expectedProduct);

            final var updateRequest = OrderHelper.updateOrderRequest(null, expectedProduct, 3.0, null);
            final var expectedUpdatedOrderResult = OrderHelper.createOrderFromUpdateOrderRequest(updateRequest, newOrderRequest, expectedProduct, null, true);

            // when
            Mockito.when(orderRepository.findByIdAndIsDeleted(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(Optional.of(expectedOrderResult));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedUpdatedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + expectedOrderResult.getId().toString()).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(newOrderRequest.getName())))

                    .andExpect(jsonPath("$.priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.priceSummary[0].amount", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary[0].currency", is(expectedUpdatedOrderResult.getPriceSummary().get(0).getCurrency().toString())))

                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))

                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getValue().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary[0].currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().get(0).getCurrency().toString())));

            verifyNoInteractions(financeApiClient);
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
    public enum NumberOperationRequest {

        equal("equal"),

        notEqual("notEqual"),

        lessThan("lessThan"),

        lessThanOrEqual("lessThanOrEqual"),

        greaterThan("greaterThan"),

        greaterThanOrEqual("greaterThanOrEqual");

        private final String value;

    }

    public static class TextAndNumberOperations implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            val textOperations = Arrays.asList(TextOperationRequest.values());
            val numberOperations = Arrays.asList(NumberOperationRequest.values());


            val result = new ArrayList<Arguments>();
            textOperations.forEach(textOperation -> {
                numberOperations.forEach(numberOperation -> {
                    result.add(Arguments.of(textOperation, numberOperation));
                });
            });
            return result.stream();
        }

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

}