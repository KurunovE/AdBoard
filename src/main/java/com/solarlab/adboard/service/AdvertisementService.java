package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.advertisement.AdvertisementCreateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementFilter;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementStatusUpdateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementUpdateRequest;
import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.enums.AdvertisementStatus;
import com.solarlab.adboard.mapper.AdvertisementMapper;
import com.solarlab.adboard.model.Advertisement;
import com.solarlab.adboard.model.Category;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.CategoryRepository;
import com.solarlab.adboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AdvertisementMapper advertisementMapper;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public List<AdvertisementResponse> findAll(AdvertisementFilter filter) {
        validateFilter(filter);

        return advertisementRepository.findAllWithFilters(
                filter.categoryId(),
                filter.authorId(),
                filter.minPrice(),
                filter.maxPrice()
        ).stream()
                .map(advertisementMapper::toAdvertisementResponse)
                .toList();
    }

    @Cacheable(value = "advertisementById", key = "#id")
    @Transactional(readOnly = true)
    public AdvertisementResponse findById(Long id) {
        return advertisementRepository.findById(id)
                .map(advertisementMapper::toAdvertisementResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + id + " not found"
                ));
    }

    @CacheEvict(value = "advertisementById", allEntries = true)
    @Transactional
    public AdvertisementResponse create(AdvertisementCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category with id " + request.categoryId() + " not found"
                ));

        User currentUser = getCurrentUser();

        Advertisement advertisement = Advertisement.builder()
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .status(AdvertisementStatus.ACTIVE)
                .author(currentUser)
                .category(category)
                .build();

        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        log.info("Created advertisement id={} for user={} in category={}",
                savedAdvertisement.getId(), currentUser.getEmail(), category.getId());
        return advertisementMapper.toAdvertisementResponse(savedAdvertisement);
    }

    @CacheEvict(value = "advertisementById", key = "#id")
    @Transactional
    public AdvertisementResponse update(Long id, AdvertisementUpdateRequest request) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + id + " not found"
                ));

        String previousTitle = advertisement.getTitle();
        BigDecimal previousPrice = advertisement.getPrice();
        Long previousCategoryId = advertisement.getCategory() != null
                ? advertisement.getCategory().getId()
                : null;

        if (hasText(request.title())) {
            advertisement.setTitle(request.title());
        }
        if (request.description() != null) {
            advertisement.setDescription(request.description());
        }
        if (request.price() != null) {
            advertisement.setPrice(request.price());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Category with id " + request.categoryId() + " not found"
                    ));
            advertisement.setCategory(category);
        }

        Advertisement updatedAdvertisement = advertisementRepository.save(advertisement);
        log.info(
                "Updated advertisement id={} titleChanged={} priceChanged={} categoryChanged={}",
                updatedAdvertisement.getId(),
                !Objects.equals(previousTitle, updatedAdvertisement.getTitle()),
                !Objects.equals(previousPrice, updatedAdvertisement.getPrice()),
                !Objects.equals(previousCategoryId, updatedAdvertisement.getCategory().getId())
        );
        return advertisementMapper.toAdvertisementResponse(updatedAdvertisement);
    }

    @CacheEvict(value = "advertisementById", key = "#id")
    @Transactional
    public AdvertisementResponse changeStatus(Long id, AdvertisementStatusUpdateRequest request) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + id + " not found"
                ));

        if (advertisement.getStatus() == request.status()) {
            log.debug("Advertisement id={} already has status={}", id, request.status());
            return advertisementMapper.toAdvertisementResponse(advertisement);
        }

        AdvertisementStatus previousStatus = advertisement.getStatus();
        advertisement.setStatus(request.status());

        Advertisement updatedAdvertisement = advertisementRepository.save(advertisement);
        log.info("Changed advertisement id={} status {} -> {}",
                updatedAdvertisement.getId(), previousStatus, updatedAdvertisement.getStatus());
        return advertisementMapper.toAdvertisementResponse(updatedAdvertisement);
    }

    @CacheEvict(value = "advertisementById", key = "#id")
    @Transactional
    public void delete(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + id + " not found"
                ));

        List<Long> imageIds = advertisement.getImages().stream()
                .map(image -> image.getId())
                .filter(Objects::nonNull)
                .toList();

        for (Long imageId : imageIds) {
            imageService.deleteImage(imageId);
        }

        advertisementRepository.deleteById(id);
        log.info("Deleted advertisement id={} with imageCount={} and commentCount={}",
                id, imageIds.size(), advertisement.getComments().size());
    }

    private User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }

        String finalEmail = email;
        return userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email " + finalEmail + " not found"
                ));
    }

    private void validateFilter(AdvertisementFilter filter) {
        if (filter.minPrice() != null
                && filter.maxPrice() != null
                && filter.minPrice().compareTo(filter.maxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
