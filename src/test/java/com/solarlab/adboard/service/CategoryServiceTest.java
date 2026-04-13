package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.category.CategoryRequest;
import com.solarlab.adboard.dto.response.category.CategoryResponse;
import com.solarlab.adboard.mapper.CategoryMapper;
import com.solarlab.adboard.model.Category;
import com.solarlab.adboard.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAllCategoriesShouldMapAll() {
        Category category = Category.builder()
                .id(1L)
                .name("Tech")
                .build();
        CategoryResponse response = new CategoryResponse(1L, "Tech", null, null);
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

        assertEquals(List.of(response), categoryService.findAllCategories());
    }

    @Test
    void findCategoryByIdShouldThrowWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.findCategoryById(1L));
    }

    @Test
    void createCategoryShouldValidateParentThenSave() {
        CategoryRequest request = new CategoryRequest("Phones", 10L);
        Category parent = Category.builder()
                .id(10L)
                .build();
        Category entity = Category.builder()
                .name("Phones")
                .build();
        Category saved = Category.builder()
                .id(1L)
                .name("Phones")
                .build();
        CategoryResponse response = new CategoryResponse(1L, "Phones", 10L, "Parent");
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(categoryMapper.toEntity(request)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(saved);
        when(categoryMapper.toCategoryResponse(saved)).thenReturn(response);

        assertEquals(response, categoryService.createCategory(request));
    }

    @Test
    void deleteCategoryShouldDeleteWhenExists() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
