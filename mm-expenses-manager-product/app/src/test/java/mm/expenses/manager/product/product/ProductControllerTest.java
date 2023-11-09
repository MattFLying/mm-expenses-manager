package mm.expenses.manager.product.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.product.ProductApplicationTest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static mm.expenses.manager.product.product.ProductHelper.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ProductControllerTest extends ProductApplicationTest {

    private static final String BASE_URL = "/products";

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class FindAll_ErrorCodes {

        @Test
        void shouldReturnBadRequest_whenPageSizeIsMissed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?pageNumber=" + 0))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPageNumberIsMissed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?pageSize=" + 1))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndPriceRangeIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=5&priceMin=1&priceMax=10"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndPriceRangeMinIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=5&priceMin=1"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndPriceRangeMaxIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=5&priceMax=10"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceAndLessThanAndGreaterThanIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=5&lessThan=true&greaterThan=true"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMinIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=1"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMaxIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMax=10"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMinAndLessThanIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=5&priceMax=20&lessThan=true"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMinAndGreaterThanIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=5&priceMax=20&greaterThan=true"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMaxAndLessThanIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=3&priceMax=15&lessThan=true"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status", Matchers.is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.occurredAt", Matchers.notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPriceMaxAndGreaterThanIsPassed() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=3&priceMax=15&greaterThan=true"))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getCode())))
                    .andExpect(jsonPath("$.message", is(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE.getMessage())))
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
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

        @Test
        void shouldReturnBadRequest_whenDetailsAsNullIsPassed() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.JPY, null);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsZero() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, BigDecimal.ZERO, CurrencyCode.AUD);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceValueIsNull() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, null, CurrencyCode.JPY);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsNull() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, null);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUnknown() throws Exception {
            // given
            final var request = createProductRequestWithUnknownCurrency(PRODUCT_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPriceCurrencyIsUndefined() throws Exception {
            // given
            final var request = createProductRequest(PRODUCT_NAME, CurrencyCode.UNDEFINED);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON));
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
        }

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            // given
            final var request = createSimpleProduct(PRODUCT_NAME);

            // when && then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        /*@Test
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
        }*/

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

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(newName)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.value", Matchers.is(newPriceValue.doubleValue())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.currency", Matchers.is(newPriceCurrency.getCode())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.details", Matchers.is(newDetails)));
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

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(newName)))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
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

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID)))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.value", Matchers.is(newPriceValue.doubleValue())))
                    .andExpect(jsonPath("$.price.currency", is(existed.getPrice().getCurrency().getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
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

                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(ID)))
                    .andExpect(jsonPath("$.name", is(existed.getName())))
                    .andExpect(jsonPath("$.price.value", is(existed.getPrice().getValue().doubleValue())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price.currency", Matchers.is(newPriceCurrency.getCode())))
                    .andExpect(jsonPath("$.details", is(existed.getDetails())));
        }

    }


    @Nested
    class DeleteProduct {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExists() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + ID))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        void shouldDeleteProduct() throws Exception {
            // given
            final var existed = createProduct();
            final var expected = existed.toBuilder().isDeleted(true).build();

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));
            Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(expected);

            // then
            mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + ID)).andExpect(MockMvcResultMatchers.status().isNoContent());
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
        void shouldFindProductById() throws Exception {
            // given
            final var existed = createProduct();

            // when
            Mockito.when(productRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(existed));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + ID).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

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
            Mockito.when(productRepository.findByName(ArgumentMatchers.eq(PRODUCT_NAME), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?name=" + PRODUCT_NAME).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByPrice_valueAndIsDeletedFalse(ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=" + priceRequest).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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
            Mockito.when(productRepository.findByNameAndPrice_value(ArgumentMatchers.eq(PRODUCT_NAME), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&name=" + PRODUCT_NAME).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByNameAndPrice_valueLessThan(ArgumentMatchers.eq(PRODUCT_NAME), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&name=" + PRODUCT_NAME + "&lessThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByNameAndPrice_valueGreaterThan(ArgumentMatchers.eq(PRODUCT_NAME), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&name=" + PRODUCT_NAME + "&greaterThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByNameAndPrice_valueBetween(ArgumentMatchers.eq(PRODUCT_NAME), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=" + 1 + "&name=" + PRODUCT_NAME + "&priceMax=" + product.getPrice().getValue().doubleValue()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByPrice_valueLessThanAndIsDeletedFalse(ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&lessThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByPrice_valueGreaterThanAndIsDeletedFalse(ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?price=" + product.getPrice().getValue().doubleValue() + "&greaterThan=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            Mockito.when(productRepository.findByPrice_valueBetweenAndIsDeletedFalse(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?priceMin=" + 1 + "&priceMax=" + product.getPrice().getValue().doubleValue()).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(1)))

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?sortOrder=NAME").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?sortOrder=PRICE_VALUE").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?sortOrder=PRICE_CURRENCY").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_2, product_1)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?sortDesc=true").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "?sortDesc=false").contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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
            final var queryFilter = Mockito.mock(ProductQueryFilter.class);
            Mockito.when(queryFilter.findFilter()).thenReturn(ProductQueryFilter.Filter.ALL);
            Mockito.when(productRepository.findAllByIsDeletedFalse(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product_1, product_2)));

            // then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL).contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", Matchers.is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", Matchers.is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)))

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