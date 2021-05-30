package mm.expenses.manager.product.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.product.ProductApplicationTest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.product.dto.request.UpdatePriceRequest;
import mm.expenses.manager.product.product.dto.request.UpdateProductRequest;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static mm.expenses.manager.product.product.ProductHelper.ID;
import static mm.expenses.manager.product.product.ProductHelper.PRODUCT_NAME;
import static mm.expenses.manager.product.product.ProductHelper.createProduct;
import static mm.expenses.manager.product.product.ProductHelper.createProductFromProductRequest;
import static mm.expenses.manager.product.product.ProductHelper.createProductFromUpdateProductRequest;
import static mm.expenses.manager.product.product.ProductHelper.createProductRequest;
import static mm.expenses.manager.product.product.ProductHelper.createProductRequestWithUnknownCurrency;
import static mm.expenses.manager.product.product.ProductHelper.updateProductRequest;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends ProductApplicationTest {

    private static final String BASE_URL = "/products";

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class FindAll_ErrorCodes {

        @Test
        void shouldReturnBadRequest_whenPageSizeIsMissed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?pageNumber=" + 0))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPageNumberIsMissed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?pageSize=" + 1))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndPriceRangeIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?price=5&priceMin=1&priceMax=10"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndPriceRangeMinIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?price=5&priceMin=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndPriceRangeMaxIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?price=5&priceMax=10"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndLessThanAndGreaterThanIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?price=5&lessThan=true&greaterThan=true"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMinIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?priceMin=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMaxIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?priceMax=10"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMinAndLessThanIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?priceMin=5&priceMax=20&lessThan=true"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMinAndGreaterThanIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?priceMin=5&priceMax=20&greaterThan=true"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMaxAndLessThanIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?priceMin=3&priceMax=15&lessThan=true"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMaxAndGreaterThanIsPassed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?priceMin=3&priceMax=15&greaterThan=true"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
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
            when(productRepository.save(any())).thenReturn(expectedResult);

            // then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(expectedResult.getId())))
                    .andExpect(jsonPath("$.name", is(expectedResult.getName())))
                    .andExpect(jsonPath("$.price.value", is(expectedResult.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(expectedResult.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.details", is(expectedResult.getDetails())));
        }

        @Test
        void shouldReturnBadRequest_whenEmptyNameIsPassed() throws Exception {
            // given
            final var emptyName = "";

            final var request = createProductRequest(emptyName, CurrencyCode.CAD);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenDetailsAsNullIsPassed() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.JPY, null);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsZero() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, BigDecimal.ZERO, CurrencyCode.AUD);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsNull() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, null, CurrencyCode.JPY);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsNull() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, null);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUnknown() throws Exception {
            // given
            final var request = createProductRequestWithUnknownCurrency(PRODUCT_NAME);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUndefined() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.UNDEFINED);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
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
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsZero() throws Exception {
            // given
            final var existed = createProduct();
            final var request = updateProductRequest(BigDecimal.ZERO, false);

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));

            // when && then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUndefined() throws Exception {
            // given
            final var existed = createProduct();
            final var request = updateProductRequest(CurrencyCode.UNDEFINED, false);

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            // given
            final var request = UpdateProductRequest.builder().name(PRODUCT_NAME).build();

            // when && then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnConflict_whenNoDataToUpdateWasPassed() throws Exception {
            // given
            final var request = UpdateProductRequest.builder().build();

            // when && then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isConflict());
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
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));
            when(productRepository.save(any())).thenReturn(expected);

            // then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.id", is(ID)))
                    .andExpect(jsonPath("$.name", is(newName)))
                    .andExpect(jsonPath("$.price.value", is(newPriceValue.doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(newPriceCurrency.getCode())))
                    .andExpect(jsonPath("$.details", is(newDetails)));
        }

        @Test
        void shouldUpdateProduct_whenOnlyNameIsPassed() throws Exception {
            // given
            final var newName = "new name";

            final var existed = createProduct();
            final var request = UpdateProductRequest.builder().name(newName).build();
            final var expected = existed.toBuilder().name(newName).build();

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));
            when(productRepository.save(any())).thenReturn(expected);

            // then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.id", is(ID)))
                    .andExpect(jsonPath("$.name", is(newName)))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
        }

        @Test
        void shouldUpdateProduct_whenOnlyPriceValueIsPassed() throws Exception {
            // given
            final var newPriceValue = BigDecimal.valueOf(5d);

            final var existed = createProduct();
            final var request = UpdateProductRequest.builder().price(UpdatePriceRequest.builder().value(newPriceValue).build()).build();
            final var expected = existed.toBuilder().price(existed.getPrice().toBuilder().value(newPriceValue).build()).build();

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));
            when(productRepository.save(any())).thenReturn(expected);

            // then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.id", is(ID)))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(jsonPath("$.price.value", is(newPriceValue.doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
        }

        @Test
        void shouldUpdateProduct_whenOnlyPriceCurrencyIsPassed() throws Exception {
            // given
            final var newPriceCurrency = CurrencyCode.USD;

            final var existed = createProduct();
            final var request = UpdateProductRequest.builder().price(UpdatePriceRequest.builder().currency(newPriceCurrency.getCode()).build()).build();
            final var expected = existed.toBuilder().price(existed.getPrice().toBuilder().currency(newPriceCurrency).build()).build();

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));
            when(productRepository.save(any())).thenReturn(expected);

            // then
            mockMvc.perform(patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.id", is(ID)))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(newPriceCurrency.getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
        }

    }


    @Nested
    class DeleteProduct {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + ID))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldDeleteProduct() throws Exception {
            // given
            final var existed = createProduct();
            final var expected = existed.toBuilder().isDeleted(true).build();

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));
            when(productRepository.save(any())).thenReturn(expected);

            // then
            mockMvc.perform(delete(BASE_URL + "/" + ID)).andExpect(status().isOk());
        }

    }


    @Nested
    class FindProductById {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + ID))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldFindProductById() throws Exception {
            // given
            final var existed = createProduct();

            // when
            when(productRepository.findById(any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(get(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.id", is(existed.getId())))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
        }

    }


    @Nested
    class FindProducts {

        @Test
        void shouldFindProductByName() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.EUR);

            // when
            when(productRepository.findByName(eq(PRODUCT_NAME), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?name=" + PRODUCT_NAME).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByPrice() throws Exception {
            // given
            final var priceRequest = 5.0d;

            final var product_1 = createProduct(PRODUCT_NAME, CurrencyCode.JPY);
            final var product_2 = createProduct(PRODUCT_NAME + " 2", CurrencyCode.AUD);

            // when
            when(productRepository.findByPrice_valueAndIsDeletedFalse(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(get(BASE_URL + "?price=" + priceRequest).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldFindProductByNameAndPrice() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.USD);

            // when
            when(productRepository.findByNameAndPrice_value(eq(PRODUCT_NAME), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&name=" + PRODUCT_NAME).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByNameAndPriceLessThan() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.USD);

            // when
            when(productRepository.findByNameAndPrice_valueLessThan(eq(PRODUCT_NAME), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&name=" + PRODUCT_NAME + "&lessThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByNameAndPriceGreaterThan() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.NZD);

            // when
            when(productRepository.findByNameAndPrice_valueGreaterThan(eq(PRODUCT_NAME), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&name=" + PRODUCT_NAME + "&greaterThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByNameAndPriceMinMax() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.SEK);

            // when
            when(productRepository.findByNameAndPrice_valueBetween(eq(PRODUCT_NAME), any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?priceMin=" + 1 + "&name=" + PRODUCT_NAME + "&priceMax=" + product.getPrice().getValue().doubleValue()).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByPriceLessThan() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.USD);

            // when
            when(productRepository.findByPrice_valueLessThanAndIsDeletedFalse(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&lessThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByPriceGreaterThan() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.NZD);

            // when
            when(productRepository.findByPrice_valueGreaterThanAndIsDeletedFalse(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&greaterThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindProductByPriceMinMax() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.SEK);

            // when
            when(productRepository.findByPrice_valueBetweenAndIsDeletedFalse(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL + "?priceMin=" + 1 + "&priceMax=" + product.getPrice().getValue().doubleValue()).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

        @Test
        void shouldFindAll() throws Exception {
            // given
            final var product = createProduct(PRODUCT_NAME, CurrencyCode.PLN);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(get(BASE_URL).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(product.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product.getDetails())));
        }

    }


    @Nested
    class FindProductsWithSorting {

        @Test
        void shouldSortByName() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.PLN);
            final var product_2 = createProduct("p2", CurrencyCode.PLN);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(get(BASE_URL + "?sortOrder=NAME").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldSortByPriceValue() throws Exception {
            // given
            final var product_1 = createProduct("p1", BigDecimal.valueOf(2), CurrencyCode.PLN);
            final var product_2 = createProduct("p2", BigDecimal.valueOf(2), CurrencyCode.PLN);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(get(BASE_URL + "?sortOrder=PRICE_VALUE").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldSortByPriceCurrency() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.AUD);
            final var product_2 = createProduct("p2", CurrencyCode.GBP);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(get(BASE_URL + "?sortOrder=PRICE_CURRENCY").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldSortDescending() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.SEK);
            final var product_2 = createProduct("p2", CurrencyCode.USD);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_2, product_1)));

            // then
            mockMvc.perform(get(BASE_URL + "?sortDesc=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_2.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_1.getDetails())));
        }

        @Test
        void shouldSortAscending() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.CAD);
            final var product_2 = createProduct("p2", CurrencyCode.AUD);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(get(BASE_URL + "?sortDesc=false").contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

        @Test
        void shouldSortAscendingByDefault() throws Exception {
            // given
            final var product_1 = createProduct("p1", CurrencyCode.CAD);
            final var product_2 = createProduct("p2", CurrencyCode.AUD);

            // when
            final var queryFilter = mock(ProductQueryFilter.class);
            when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            when(productRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(get(BASE_URL).contentType(DATA_FORMAT_JSON))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpect(jsonPath("$.hasNext", is(false)))
                    .andExpect(jsonPath("$.isFirst", is(true)))
                    .andExpect(jsonPath("$.isLast", is(true)))
                    .andExpect(jsonPath("$.content", hasSize(2)))

                    .andExpect(jsonPath("$.content[0].id", is(product_1.getId())))
                    .andExpect(jsonPath("$.content[0].name", is(product_1.getName())))
                    .andExpect(jsonPath("$.content[0].price.value", is(product_1.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[0].price.currency", is(product_1.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].details", is(product_1.getDetails())))

                    .andExpect(jsonPath("$.content[1].id", is(product_2.getId())))
                    .andExpect(jsonPath("$.content[1].name", is(product_2.getName())))
                    .andExpect(jsonPath("$.content[1].price.value", is(product_2.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.content[1].price.currency", is(product_2.getPrice().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].details", is(product_2.getDetails())));
        }

    }

}