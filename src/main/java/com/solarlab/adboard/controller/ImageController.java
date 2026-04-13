package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.response.ExceptionResponse;
import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.mapper.ImageMapper;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/images")
@Tag(name = "Images", description = "Operations with advertisement images")
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper imageMapper;

    @Operation(summary = "Upload image for advertisement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid multipart request",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/advertisements/{advertisementId}/upload")
    @PreAuthorize("@securityUtils.isAdvertisementOwner(#advertisementId)")
    public ResponseEntity<ImageResponse> uploadImage(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId,
            @RequestParam("file") MultipartFile file
    ) {
        Image image = imageService.uploadImage(file, advertisementId);
        return ResponseEntity.ok(imageMapper.toImageResponse(image));
    }

    @Operation(summary = "Delete image")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid id",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PreAuthorize("@securityUtils.isImageOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
