package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.CategoryRequest;
import com.solarlab.adboard.dto.response.CategoryResponse;
import com.solarlab.adboard.mapper.CategoryMapper;
import com.solarlab.adboard.model.Category;
import com.solarlab.adboard.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category with id " + id + " not found"
                ));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        if (categoryRequest.parentId() != null) {
            categoryRepository.findById(categoryRequest.parentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Category with id " + categoryRequest.parentId() + " not found"
                    ));
        }

        Category category = categoryMapper.toEntity(categoryRequest);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category with id " + id + " not found");
        }
        categoryRepository.deleteById(id);
    }
}
