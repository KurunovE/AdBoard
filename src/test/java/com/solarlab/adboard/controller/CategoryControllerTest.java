package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.category.CategoryRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void categoryControllerShouldDelegate() {
        CategoryResponse response = new CategoryResponse(
                1L, "Tech", null, null
        );
        when(categoryService.findAllCategories()).thenReturn(List.of(response));
        when(categoryService.findCategoryById(1L)).thenReturn(response);
        when(categoryService.createCategory(new CategoryRequest("Tech", null)))
                .thenReturn(response);

        assertEquals(1, categoryController.getCategories().getBody().size());
        assertEquals(response, categoryController.getCategoryById(1L).getBody());
        assertEquals(response, categoryController.createCategory(new CategoryRequest(
                "Tech", null)).getBody()
        );
        assertEquals(HttpStatus.NO_CONTENT, categoryController.deleteCategory(1L).getStatusCode());
    }
}
