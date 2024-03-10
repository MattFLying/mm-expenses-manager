package mm.expenses.manager.order.order;

import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.web.exception.ExceptionMessage;
import mm.expenses.manager.order.OrderApplicationTest;
import mm.expenses.manager.order.api.order.model.OrderIds;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static mm.expenses.manager.order.order.OrderHelper.ID;
import static mm.expenses.manager.order.order.OrderHelper.ORDER_NAME;
import static mm.expenses.manager.order.order.OrderWebApi.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class OrderControllerTest extends OrderApplicationTest {

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<List<Order>> ordersCaptor;

    @Nested
    class FindAll_ErrorCodes {

        @Test
        void shouldReturnBadRequest_whenPageSizeIsMissed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(PaginationConfig.PAGE_NUMBER_PROPERTY, "0"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(OrderExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(OrderExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPageNumberIsMissed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(PaginationConfig.PAGE_SIZE_PROPERTY, "1"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(OrderExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(OrderExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceSummaryLessAndGreaterUsed() throws Exception {
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of("1.5"));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(OrderExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getCode())))
                    .andExpect(jsonPath("$.message", is(OrderExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceSummaryHasMissingLessOrGreater() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, "2.5"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(OrderExceptionMessage.PRICE_MUST_BE_LESS_THAN_OR_GREATER_THAN.getCode())))
                    .andExpect(jsonPath("$.message", is(OrderExceptionMessage.PRICE_MUST_BE_LESS_THAN_OR_GREATER_THAN.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenProductsCountLessAndGreaterUsed() throws Exception {
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of("2"));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_LESS_THAN_PROPERTY, List.of("true"));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(OrderExceptionMessage.PRODUCTS_COUNT_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getCode())))
                    .andExpect(jsonPath("$.message", is(OrderExceptionMessage.PRODUCTS_COUNT_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

    }


    @Nested
    class FindAll {

        @Test
        void shouldFindAll() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            Mockito.when(orderRepository.findAllNotDeleted(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byName() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);

            // when
            Mockito.when(orderRepository.findByNameAndNotDeleted(eq(ORDER_NAME), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderQueryFilter.NAME_PROPERTY, ORDER_NAME).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byPriceLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByPriceSummaryLessThanAndNotDeleted(eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(priceSummary.toString()));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byPriceGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByPriceSummaryGreaterThanAndNotDeleted(eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(priceSummary.toString()));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byNameAndPriceLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByNameAndPriceSummaryLessThanAndNotDeleted(eq(ORDER_NAME), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.NAME_PROPERTY, List.of(ORDER_NAME));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(priceSummary.toString()));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byNameAndPriceGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByNameAndPriceSummaryGreaterThanAndNotDeleted(eq(ORDER_NAME), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.NAME_PROPERTY, List.of(ORDER_NAME));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(priceSummary.toString()));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byNameAndProductsCount() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();

            // when
            Mockito.when(orderRepository.findByNameAndProductsCountAndNotDeleted(eq(ORDER_NAME), eq(productsCount), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.NAME_PROPERTY, List.of(ORDER_NAME));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byNameAndProductsCountAndPriceLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByNameAndProductsCountAndPriceSummaryLessThanAndNotDeleted(eq(ORDER_NAME), eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.NAME_PROPERTY, List.of(ORDER_NAME));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byNameAndProductsCountAndPriceGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByNameAndProductsCountAndPriceSummaryGreaterThanAndNotDeleted(eq(ORDER_NAME), eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.NAME_PROPERTY, List.of(ORDER_NAME));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCount() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();

            // when
            Mockito.when(orderRepository.findByProductsCountAndNotDeleted(eq(productsCount), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).param(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, String.valueOf(productsCount)).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();

            // when
            Mockito.when(orderRepository.findByProductsCountLessThanAndNotDeleted(eq(productsCount), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();

            // when
            Mockito.when(orderRepository.findByProductsCountGreaterThanAndNotDeleted(eq(productsCount), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountAndPriceLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByProductsCountAndPriceSummaryLessThanAndNotDeleted(eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountAndPriceGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByProductsCountAndPriceSummaryGreaterThanAndNotDeleted(eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountLessThanAndPriceLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByProductsCountLessThanAndPriceSummaryLessThanAndNotDeleted(eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_LESS_THAN_PROPERTY, List.of("true"));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountLessThanAndPriceGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByProductsCountLessThanAndPriceSummaryGreaterThanAndNotDeleted(eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_LESS_THAN_PROPERTY, List.of("true"));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountGreaterThanAndPriceLessThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByProductsCountGreaterThanAndPriceSummaryLessThanAndNotDeleted(eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_GREATER_THAN_PROPERTY, List.of("true"));
            params.put(OrderQueryFilter.PRICE_SUMMARY_LESS_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldFindAll_byProductsCountGreaterThanAndPriceGreaterThan() throws Exception {
            // given
            final var expectedProduct = OrderHelper.createProduct();
            final var request = OrderHelper.createOrderRequest(ORDER_NAME, expectedProduct);
            final var expectedOrderResult = OrderHelper.createOrderFromOrderRequest(request, expectedProduct);
            final var productsCount = expectedOrderResult.getProducts().size();
            final var priceSummary = expectedOrderResult.getPriceSummary().getAmount();

            // when
            Mockito.when(orderRepository.findByProductsCountGreaterThanAndPriceSummaryGreaterThanAndNotDeleted(eq(productsCount), eq(priceSummary), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedOrderResult)));

            // then
            final var params = new LinkedMultiValueMap<String, String>();
            params.put(OrderQueryFilter.PRODUCTS_COUNT_PROPERTY, List.of(String.valueOf(productsCount)));
            params.put(OrderQueryFilter.PRICE_SUMMARY_PROPERTY, List.of(String.valueOf(priceSummary)));
            params.put(OrderQueryFilter.PRODUCTS_COUNT_GREATER_THAN_PROPERTY, List.of("true"));
            params.put(OrderQueryFilter.PRICE_SUMMARY_GREATER_THAN_PROPERTY, List.of("true"));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).params(params).contentType(DATA_FORMAT_JSON))
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
                    .andExpect(jsonPath("$.content[0].priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.content[0].orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + expectedOrderResult.getId().toString()).param(OrderQueryFilter.IS_DELETED_PROPERTY, String.valueOf(true)).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + expectedOrderResult.getId().toString()).param(OrderQueryFilter.IS_DELETED_PROPERTY, String.valueOf(false)).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

        @Test
        void shouldNotFindOrderById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
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

            // when
            Mockito.when(productRepository.findByIdIn(ArgumentMatchers.any())).thenReturn(List.of(expectedProduct));
            Mockito.when(orderRepository.save(ArgumentMatchers.any())).thenReturn(expectedOrderResult);

            // then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.id", is(expectedOrderResult.getId().toString())))
                    .andExpect(jsonPath("$.name", is(expectedOrderResult.getName())))
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
        }

        @Test
        void shouldReturnBadRequest_whenNullProductsArePassed() throws Exception {
            // given
            final var request = OrderHelper.createOrderRequest(null, ORDER_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

        @Test
        void shouldReturnBadRequest_whenEmptyProductsArePassed() throws Exception {
            // given
            final var request = OrderHelper.createOrderRequest(List.of(), ORDER_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
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
        }

        @Test
        void shouldReturnBadRequest_whenNoIdForProductIsPassed() throws Exception {
            // given
            final var request = OrderHelper.createOrderRequestEmptyProductId(ORDER_NAME);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(2)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts[1].id", is(expectedProductToAdd.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[1].quantity", is(expectedUpdatedOrderResult.getProducts().get(1).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(0)));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProductToAdd.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(2)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts[1].id", is(expectedProductToAdd.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[1].quantity", is(expectedUpdatedOrderResult.getProducts().get(1).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[1].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(1).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
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
                    .andExpect(jsonPath("$.priceSummary.amount", is(expectedUpdatedOrderResult.getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.priceSummary.currency", is(expectedUpdatedOrderResult.getPriceSummary().getCurrency().toString())))
                    .andExpect(jsonPath("$.orderedProducts", hasSize(1)))
                    .andExpect(jsonPath("$.orderedProducts[0].id", is(expectedProduct.getId().toString())))
                    .andExpect(jsonPath("$.orderedProducts[0].quantity", is(expectedUpdatedOrderResult.getProducts().get(0).getQuantity())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.amount", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getAmount().doubleValue())))
                    .andExpect(jsonPath("$.orderedProducts[0].priceSummary.currency", is(expectedUpdatedOrderResult.getProducts().get(0).getPriceSummary().getCurrency().toString())));
        }

    }

}