package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.advertisement.AdvertisementCreateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementFilter;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AdvertisementMapper advertisementMapper;

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

    @Transactional(readOnly = true)
    public AdvertisementResponse findById(Long id) {
        return advertisementRepository.findById(id)
                .map(advertisementMapper::toAdvertisementResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + id + " not found"
                ));
    }

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
        return advertisementMapper.toAdvertisementResponse(savedAdvertisement);
    }

    @Transactional
    public AdvertisementResponse update(Long id, AdvertisementUpdateRequest request) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + id + " not found"
                ));

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
        return advertisementMapper.toAdvertisementResponse(updatedAdvertisement);
    }

    @Transactional
    public void delete(Long id) {
        if (!advertisementRepository.existsById(id)) {
            throw new EntityNotFoundException("Advertisement with id " + id + " not found");
        }
        advertisementRepository.deleteById(id);
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
