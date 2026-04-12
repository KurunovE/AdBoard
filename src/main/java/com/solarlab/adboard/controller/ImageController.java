package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.mapper.ImageMapper;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.service.ImageService;
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
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper imageMapper;

    @PostMapping("/advertisements/{advertisementId}/upload")
    @PreAuthorize("@securityUtils.isAdvertisementOwner(#advertisementId)")
    public ResponseEntity<ImageResponse> uploadImage(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId,
            @RequestParam("file") MultipartFile file
    ) {
        Image image = imageService.uploadImage(file, advertisementId);
        return ResponseEntity.ok(imageMapper.toImageResponse(image));
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityUtils.isImageOwner(#id)")
    public ResponseEntity<Void> deleteImage(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
