package com.solarlab.adboard.repository;

import com.solarlab.adboard.model.Advertisement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @EntityGraph(attributePaths = {"category", "author", "images"})
    @Query("SELECT a FROM Advertisement a " +
           "WHERE (:categoryId IS NULL OR a.category.id = :categoryId) " +
           "AND (:authorId IS NULL OR a.author.id = :authorId) " +
           "AND (:minPrice IS NULL OR a.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR a.price <= :maxPrice)")
    List<Advertisement> findAllWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("authorId") Long authorId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    @EntityGraph(attributePaths = {"category", "author", "images"})
    @Override
    Optional<Advertisement> findById(Long id);
}
