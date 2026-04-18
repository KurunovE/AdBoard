package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.category.CategoryRequest;
import com.solarlab.adboard.dto.request.category.CategoryUpdateRequest;
import com.solarlab.adboard.dto.response.category.CategoryResponse;
import com.solarlab.adboard.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void categoryControllerShouldDelegate() {
        CategoryRequest request = new CategoryRequest("Tech", null);
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("Updated tech", 2L);
        CategoryResponse response = new CategoryResponse(
                1L, "Tech", null
        );
        when(categoryService.findAllCategories()).thenReturn(List.of(response));
        when(categoryService.findCategoryById(1L)).thenReturn(response);
        when(categoryService.createCategory(request)).thenReturn(response);
        when(categoryService.updateCategory(1L, updateRequest)).thenReturn(response);

        assertEquals(1, categoryController.getCategories().getBody().size());
        assertEquals(response, categoryController.getCategoryById(1L).getBody());
        assertEquals(response, categoryController.createCategory(request).getBody());
        assertEquals(response, categoryController.updateCategory(1L, updateRequest).getBody());
        assertEquals(HttpStatus.NO_CONTENT, categoryController.deleteCategory(1L).getStatusCode());
        verify(categoryService).deleteCategory(1L);
    }
}
