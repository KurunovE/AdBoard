package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.CategoryRequest;
import com.solarlab.adboard.dto.response.CategoryResponse;
import com.solarlab.adboard.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.findAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(categoryService.findCategoryById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        return ResponseEntity.ok(categoryService.createCategory(categoryRequest));
    }

    public void deleteCategory(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        categoryService.deleteCategory(id);
    }
}
