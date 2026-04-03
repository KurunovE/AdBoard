package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.request.CategoryRequest;
import com.solarlab.adboard.dto.response.CategoryResponse;
import com.solarlab.adboard.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "mapParentById")
    Category toEntity(CategoryRequest request);

    @Named("mapParentById")
    default Category mapParentById(Long parentId) {
        if (parentId == null) {
            return null;
        }
        Category parent = new Category();
        parent.setId(parentId);
        return parent;
    }
}
