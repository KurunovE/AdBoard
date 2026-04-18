package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.category.CategoryRequest;
import com.solarlab.adboard.dto.request.category.CategoryUpdateRequest;
import com.solarlab.adboard.dto.response.category.CategoryResponse;
import com.solarlab.adboard.mapper.CategoryMapper;
import com.solarlab.adboard.model.Category;
import com.solarlab.adboard.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Cacheable("categories")
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Cacheable(value = "categoryById", key = "#id")
    @Transactional(readOnly = true)
    public CategoryResponse findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category with id " + id + " not found"
                ));
    }

    @CacheEvict(value = {"categories", "categoryById"}, allEntries = true)
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
        log.info("Created category id={} name={} parentId={}",
                savedCategory.getId(), savedCategory.getName(), categoryRequest.parentId());

        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @CacheEvict(value = {"categories", "categoryById"}, allEntries = true)
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category with id " + id + " not found"
                ));

        if (Objects.equals(id, categoryUpdateRequest.parentId())) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }

        if (hasText(categoryUpdateRequest.name())) {
            category.setName(categoryUpdateRequest.name());
        }

        if (categoryUpdateRequest.parentId() != null) {
            Category parent = categoryRepository.findById(categoryUpdateRequest.parentId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Category with id " + categoryUpdateRequest.parentId() + " not found"
                    ));
            category.setParent(parent);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category id={} name={} parentId={}",
                updatedCategory.getId(),
                updatedCategory.getName(),
                updatedCategory.getParent() != null ? updatedCategory.getParent().getId() : null);

        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @CacheEvict(value = {"categories", "categoryById"}, allEntries = true)
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category with id " + id + " not found");
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category id={}", id);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
