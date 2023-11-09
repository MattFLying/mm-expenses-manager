package mm.expenses.manager.order.product;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.order.api.product.model.CreateNewProductRequest;
import mm.expenses.manager.order.api.product.model.ProductPage;
import mm.expenses.manager.order.api.product.model.ProductResponse;
import mm.expenses.manager.order.api.product.model.UpdateProductRequest;
import mm.expenses.manager.order.currency.PriceMapper;
import mm.expenses.manager.order.product.model.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {PriceMapper.class})
abstract class ProductMapper extends AbstractMapper {

    @Mapping(target = "name", source = "newProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    abstract Product map(final CreateNewProductRequest newProduct, final Instant creationTime);

    @Mapping(target = "name", source = "updateProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "price", source = "updateProduct.price")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(createInstantNow())")
    abstract Product map(final UpdateProductRequest updateProduct, final ProductEntity entity);

    abstract ProductEntity map(final Product domain);

    abstract Product map(final ProductEntity entity);

    abstract CreateNewProductRequest mapToNewRequest(CreateNewProductRequest request);

    abstract UpdateProductRequest mapToUpdatedRequest(UpdateProductRequest request);

    abstract ProductResponse mapToResponse(final Product domain);

    ProductPage mapToPageResponse(final Page<Product> productPage) {
        final var content = productPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        var page = new ProductPage();
        page.content(content);
        page.empty(CollectionUtils.isEmpty(content));
        page.setTotalElements(productPage.getTotalElements());
        page.setTotalPages(productPage.getTotalPages());
        page.setFirst(productPage.isFirst());
        page.setLast(productPage.isLast());
        page.setHasNext(productPage.hasNext());
        page.page(productPage.getNumber());
        page.size(productPage.getSize());
        page.elements(productPage.getNumberOfElements());

        return page;
    }

}

