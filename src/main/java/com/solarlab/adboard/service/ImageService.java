package com.solarlab.adboard.service;

import com.solarlab.adboard.model.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image uploadImage(MultipartFile file, Long advertisementId);
    void deleteImage(Long imageId);
}
