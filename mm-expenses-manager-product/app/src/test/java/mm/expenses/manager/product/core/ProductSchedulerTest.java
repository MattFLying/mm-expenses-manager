package mm.expenses.manager.product.core;

import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.product.ProductApplicationTest;
import mm.expenses.manager.product.currency.CurrencyMapper;
import mm.expenses.manager.product.currency.CurrencyMapperImpl;
import mm.expenses.manager.product.price.ProductPrice;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static mm.expenses.manager.product.currency.PriceConverter.CORRELATION_ID_SEPARATOR;
import static mm.expenses.manager.product.processor.ProductHelper.createProduct;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

class ProductSchedulerTest extends ProductApplicationTest {

    @Autowired
    private ProductScheduler productScheduler;

    @Autowired
    private PaginationHelper pagination;

    @MockBean
    private ProductAsyncHandler asyncHandler;

    private final CurrencyMapper mapper = mock(CurrencyMapperImpl.class);

    @Test
    void shouldDeleteMarkedAsDeleted() {
        // given
        final var existed_1 = createProduct().toBuilder().id(UUID.randomUUID()).isDeleted(true).build();
        final var existed_2 = createProduct().toBuilder().id(UUID.randomUUID()).isDeleted(true).build();

        // when
        Mockito.when(productRepository.findAllByIsDeletedTrue(eq(pagination.getPageRequest(0, 50)))).thenReturn(new PageImpl<>(List.of(existed_1, existed_2)));
        Mockito.when(productRepository.findAllByIsDeletedTrue(eq(pagination.getPageRequest(1, 50)))).thenReturn(new PageImpl<>(List.of()));

        productScheduler.cleanDeletedProducts();

        // then
        Mockito.verify(productRepository, Mockito.times(2)).findAllByIsDeletedTrue(ArgumentMatchers.any());
        Mockito.verify(productRepository).deleteByIdIn(ArgumentMatchers.anyList());
    }

    @Test
    void shouldUpdateCurrenciesForProductsPrices() {
        // given
        final var product = createProduct().toBuilder().id(UUID.randomUUID()).build();
        final var availableCurrenciesSize = CurrencyCode.available().size();
        final var conversionProduct = createCurrencyConversionResponses(product);
        final var convertedPricesResponse = new ArrayList<>(conversionProduct);

        // when
        Mockito.when(productRepository.findAllWithCurrenciesLessThan(eq(availableCurrenciesSize), eq(pagination.getPageRequest(0, 50)))).thenReturn(new PageImpl<>(List.of(product)));
        Mockito.when(productRepository.findAllWithCurrenciesLessThan(eq(availableCurrenciesSize), eq(pagination.getPageRequest(1, 50)))).thenReturn(new PageImpl<>(List.of()));
        Mockito.when(mapper.map(any(), any(), any(), any())).thenCallRealMethod();
        Mockito.when(financeApiClient.convertMultipleRates(any())).thenReturn(convertedPricesResponse);
        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        productScheduler.updateCurrenciesForProductsPrices();

        // then
        Mockito.verify(productRepository, Mockito.times(2)).findAllWithCurrenciesLessThan(eq(availableCurrenciesSize), ArgumentMatchers.any());
        Mockito.verify(financeApiClient, Mockito.times(1)).convertMultipleRates(ArgumentMatchers.any());
        Mockito.verify(productRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verify(asyncHandler, Mockito.times(1)).sendProductPricesMessages(ArgumentMatchers.any(), ArgumentMatchers.any(), eq(AsyncKafkaOperation.UPDATE));
    }

    private List<CurrencyConversionResponse> createCurrencyConversionResponses(final Product product) {
        return CurrencyCode.available().stream()
                .map(currencyCode -> {
                    final var response = new CurrencyConversionResponse();
                    response.setId(String.format("%s%s%s", product.getId().toString(), CORRELATION_ID_SEPARATOR, currencyCode.getCode()));
                    response.setDate(DateUtils.fromStringToLocalDate(product.getPrices().get(0).getDate()));

                    final var valueDtoTo = new CurrencyConversionValueDto();
                    valueDtoTo.setCode(product.getPrices().get(0).getCurrency().getCode());
                    valueDtoTo.setValue(product.getPrices().get(0).getValue().doubleValue());

                    response.setTo(valueDtoTo);

                    return response;
                })
                .toList();
    }

    private Product updatedProduct(final Product product, final List<CurrencyConversionResponse> convertedPricesResponse) {
        final var updatedProduct = createProduct().toBuilder().id(product.getId()).build();

        convertedPricesResponse.stream()
                .map(price -> ProductPrice.builder()
                        .product(product)
                        .value(BigDecimalWrapper.of(price.getTo().getValue()))
                        .currency(CurrencyCode.getCurrencyFromString(price.getTo().getCode()))
                        .build()
                )
                .forEach(productPrice -> updatedProduct.addPrice(productPrice));

        return updatedProduct;
    }

}