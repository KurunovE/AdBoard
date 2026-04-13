package com.solarlab.adboard.repository;

import com.solarlab.adboard.model.Image;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @EntityGraph(attributePaths = {"advertisement", "advertisement.author"})
    List<Image> findAllByAdvertisementIdOrderBySortOrderAsc(Long advertisementId);

    @EntityGraph(attributePaths = {"advertisement", "advertisement.author"})
    @Override
    Optional<Image> findById(Long id);
}
