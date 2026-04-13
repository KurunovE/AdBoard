package com.solarlab.adboard.service;

import com.solarlab.adboard.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<Image> getAdvertisementImages(Long advertisementId);
    Image uploadImage(MultipartFile file, Long advertisementId);
    void deleteImage(Long imageId);
}
