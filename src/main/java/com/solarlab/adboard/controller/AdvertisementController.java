package com.solarlab.adboard.controller;

import com.solarlab.adboard.config.SecurityUtils;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementCreateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementFilter;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementStatusUpdateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementUpdateRequest;
import com.solarlab.adboard.dto.response.ExceptionResponse;
import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/advertisements")
@Tag(name = "Advertisements", description = "Operations with advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Get advertisements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advertisements returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
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

    @Operation(summary = "Get advertisement by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advertisement returned"),
            @ApiResponse(responseCode = "400", description = "Invalid id",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> getAdvertisementById(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(advertisementService.findById(id));
    }

    @Operation(summary = "Create advertisement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advertisement created"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AdvertisementResponse> createAdvertisement(
            @Valid @RequestBody AdvertisementCreateRequest request
    ) {
        return ResponseEntity.ok(advertisementService.create(request));
    }

    @Operation(summary = "Update advertisement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advertisement updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement or category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> updateAdvertisement(
            @PositiveOrZero @PathVariable(name = "id") Long id,
            @Valid @RequestBody AdvertisementUpdateRequest request
    ) {
        securityUtils.ensureAdvertisementOwner(id);
        return ResponseEntity.ok(advertisementService.update(id, request));
    }

    @Operation(summary = "Change advertisement status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdvertisementResponse> changeAdvertisementStatus(
            @PositiveOrZero @PathVariable(name = "id") Long id,
            @Valid @RequestBody AdvertisementStatusUpdateRequest request
    ) {
        securityUtils.ensureAdvertisementOwner(id);
        return ResponseEntity.ok(advertisementService.changeStatus(id, request));
    }

    @Operation(summary = "Delete advertisement")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Advertisement deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid id",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        securityUtils.ensureAdvertisementOwner(id);
        advertisementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
