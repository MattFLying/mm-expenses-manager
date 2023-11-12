package mm.expenses.manager.product.product;

import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.product.ProductApplicationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static mm.expenses.manager.product.product.ProductHelper.createProduct;
import static org.mockito.ArgumentMatchers.eq;

class ProductSchedulerTest extends ProductApplicationTest {

    @Autowired
    private ProductScheduler productScheduler;

    @Autowired
    private PaginationHelper pagination;

    @Test
    void shouldDeleteMarkedAsDeleted() {
        // given
        final var existed_1 = createProduct().toBuilder().id(UUID.randomUUID().toString()).isDeleted(true).build();
        final var existed_2 = createProduct().toBuilder().id(UUID.randomUUID().toString()).isDeleted(true).build();

        // when
        Mockito.when(productRepository.findAllByIsDeletedTrue(eq(pagination.getPageRequest(0, 50)))).thenReturn(new PageImpl<>(List.of(existed_1, existed_2)));
        Mockito.when(productRepository.findAllByIsDeletedTrue(eq(pagination.getPageRequest(1, 50)))).thenReturn(new PageImpl<>(List.of()));

        productScheduler.cleanDeletedProducts();

        // then
        Mockito.verify(productRepository, Mockito.times(2)).findAllByIsDeletedTrue(ArgumentMatchers.any());
        Mockito.verify(productRepository).deleteByIdIn(ArgumentMatchers.anyList());
    }

}