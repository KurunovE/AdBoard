package com.solarlab.adboard.repository;

import com.solarlab.adboard.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
