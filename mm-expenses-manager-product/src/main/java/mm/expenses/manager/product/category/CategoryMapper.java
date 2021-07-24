package mm.expenses.manager.product.category;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.product.category.command.CreateCategoryCommand;
import mm.expenses.manager.product.category.command.CreateCategoryCommand.CreateSubcategoryCommand;
import mm.expenses.manager.product.category.dto.request.CreateCategoryRequest;
import mm.expenses.manager.product.category.dto.request.CreateCategoryRequest.CreateSubcategoryRequest;
import mm.expenses.manager.product.category.dto.response.CategoryResponse;
import mm.expenses.manager.product.config.MapperImplNaming;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.CATEGORY_MAPPER)
abstract class CategoryMapper extends AbstractMapper {

    @Mapping(target = "subcategories", expression = "java(map(createCategoryRequest.getSubcategories()))")
    abstract CreateCategoryCommand map(final CreateCategoryRequest createCategoryRequest);

    abstract CreateSubcategoryCommand map(final CreateSubcategoryRequest createSubcategoryRequest);

    @Mapping(target = "isParent", expression = "java(category.isParent())")
    @Mapping(target = "parentId", expression = "java(getParentId(category))")
    abstract CategoryResponse map(final Category category);

    abstract Collection<CreateSubcategoryCommand> map(final Collection<CreateSubcategoryRequest> createSubcategoriesRequest);

    protected String getParentId(final Category category) {
        return !category.isParent()
                ? category.getParentId()
                : null;
    }

}
