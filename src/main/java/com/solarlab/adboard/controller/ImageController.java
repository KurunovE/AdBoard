package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.mapper.ImageMapper;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.service.ImageService;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper imageMapper;

    @PostMapping("/advertisements/{advertisementId}/upload")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ImageResponse> uploadImage(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId,
            @RequestParam("file") MultipartFile file
    ) {
        Image image = imageService.uploadImage(file, advertisementId);
        return ResponseEntity.ok(imageMapper.toImageResponse(image));
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
