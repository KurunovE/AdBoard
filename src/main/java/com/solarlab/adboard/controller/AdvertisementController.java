package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.advertisement.AdvertisementCreateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementFilter;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementUpdateRequest;
import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.service.AdvertisementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping
    public ResponseEntity<List<AdvertisementResponse>> getAdvertisements(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ResponseEntity.ok(advertisementService.findAll(
                new AdvertisementFilter(categoryId, authorId, minPrice, maxPrice)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> getAdvertisementById(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(advertisementService.findById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<AdvertisementResponse> createAdvertisement(
            @Valid @RequestBody AdvertisementCreateRequest request
    ) {
        return ResponseEntity.ok(advertisementService.create(request));
    }

    @PreAuthorize("@securityUtils.isAdvertisementOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> updateAdvertisement(
            @PositiveOrZero @PathVariable(name = "id") Long id,
            @Valid @RequestBody AdvertisementUpdateRequest request
    ) {
        return ResponseEntity.ok(advertisementService.update(id, request));
    }

    @PreAuthorize("@securityUtils.isAdvertisementOwner(#id)")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteAdvertisement(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        advertisementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
