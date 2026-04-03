package com.solarlab.adboard.service.imageServiceImpl;

import com.solarlab.adboard.model.Advertisement;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.ImageRepository;
import com.solarlab.adboard.service.ImageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class YandexDriveImageService implements ImageService {

    private final RestTemplate yandexRestTemplate;
    private final ImageRepository imageRepository;
    private final AdvertisementRepository advertisementRepository;

    @Value("${yandex.disk.api-url}")
    private String apiUrl;

    public YandexDriveImageService(@Qualifier("yandexRestTemplate") RestTemplate yandexRestTemplate,
                                   ImageRepository imageRepository,
                                   AdvertisementRepository advertisementRepository) {
        this.yandexRestTemplate = yandexRestTemplate;
        this.imageRepository = imageRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Override
    @Transactional
    public Image uploadImage(MultipartFile file, Long advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement not found"
                ));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String path = "app:/images/" + fileName;

        try {
            String uploadUrl = getUploadUrl(path);

            uploadFileToYandex(uploadUrl, file.getBytes());

            publishFile(path);

            String publicUrl = getPublicUrl(path);

            Image image = Image.builder()
                    .advertisement(advertisement)
                    .url(publicUrl)
                    .path(path)
                    .sortOrder(advertisement.getImages().size())
                    .build();

            return imageRepository.save(image);

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Image not found"
                ));

        try {
            deleteFromYandex(image.getPath());

            imageRepository.delete(image);
        } catch (Exception e) {
            log.error("Failed to delete image from Yandex Disk", e);
            throw new RuntimeException("Image deletion failed", e);
        }
    }

    private String getUploadUrl(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl + "/upload")
                .queryParam("path", path)
                .toUriString();

        ResponseEntity<Map> response = yandexRestTemplate.getForEntity(url, Map.class);
        return (String) response.getBody().get("href");
    }

    private void uploadFileToYandex(String uploadUrl, byte[] fileData) {
        yandexRestTemplate.put(uploadUrl, fileData);
    }

    private void publishFile(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl + "/publish")
                .queryParam("path", path)
                .toUriString();

        yandexRestTemplate.exchange(url, HttpMethod.PUT, null, Void.class);
    }

    private String getPublicUrl(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("path", path)
                .queryParam("fields", "public_url")
                .toUriString();

        ResponseEntity<Map> response = yandexRestTemplate.getForEntity(url, Map.class);
        return (String) response.getBody().get("public_url");
    }

    private void deleteFromYandex(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("path", path)
                .toUriString();

        yandexRestTemplate.delete(url);
    }
}
