package com.solarlab.adboard.service.imageServiceImpl;

import com.solarlab.adboard.dto.response.yandex.YandexPublicUrlResponse;
import com.solarlab.adboard.dto.response.yandex.YandexUploadResponse;
import com.solarlab.adboard.exception.YandexDiskException;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class YandexDriveImageService implements ImageService {

    private final RestTemplate yandexRestTemplate;
    private final ImageRepository imageRepository;
    private final AdvertisementRepository advertisementRepository;

    @Value("${yandex.disk.api-url}")
    private String apiUrl;

    public YandexDriveImageService(
            @Qualifier("yandexRestTemplate") RestTemplate yandexRestTemplate,
            ImageRepository imageRepository,
            AdvertisementRepository advertisementRepository
    ) {
        this.yandexRestTemplate = yandexRestTemplate;
        this.imageRepository = imageRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Override
    @Transactional
    public Image uploadImage(MultipartFile file, Long advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + advertisementId + " not found"
                ));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String directoryPath = "app:/images";
        String filePath = directoryPath + "/" + fileName;

        try {
            ensureDirectoryExists(directoryPath);

            String uploadUrl = getUploadUrl(filePath);

            uploadFileToYandex(uploadUrl, file.getBytes());

            publishFile(filePath);

            String publicUrl = getPublicUrl(filePath);

            Image image = Image.builder()
                    .advertisement(advertisement)
                    .url(publicUrl)
                    .path(filePath)
                    .sortOrder(advertisement.getImages().size())
                    .build();

            return imageRepository.save(image);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new YandexDiskException(
                    e.getMessage(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
        } catch (IOException e) {
            log.error("File processing error during upload", e);
            throw new RuntimeException("Failed to read image file", e);
        }
    }

    private void ensureDirectoryExists(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("path", path)
                .toUriString();
        try {
            yandexRestTemplate.getForEntity(url, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Directory {} not found on Yandex Disk, creating it", path);
            try {
                yandexRestTemplate.put(url, null);
            } catch (HttpClientErrorException.Conflict conflict) {
                log.info("Directory {} already exists (concurrent creation)", path);
            } catch (HttpClientErrorException | HttpServerErrorException e2) {
                throw new YandexDiskException("Failed to create directory: " + e2.getMessage(),
                        e2.getStatusCode(), e2.getResponseBodyAsString());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new YandexDiskException("Failed to check directory: " + e.getMessage(),
                    e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Image with: " + imageId + " not found"
                ));

        try {
            deleteFromYandex(image.getPath());
            imageRepository.delete(image);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new YandexDiskException(
                    e.getMessage(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
        }
    }

    private String getUploadUrl(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl + "/upload")
                .queryParam("path", path)
                .toUriString();

        ResponseEntity<YandexUploadResponse> response = yandexRestTemplate.getForEntity(url,
                YandexUploadResponse.class);

        return Objects.requireNonNull(response.getBody()).href();
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

        ResponseEntity<YandexPublicUrlResponse> response =
                yandexRestTemplate.getForEntity(url, YandexPublicUrlResponse.class);
        return Objects.requireNonNull(response.getBody()).publicUrl();
    }

    private void deleteFromYandex(String path) {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("path", path)
                .toUriString();

        yandexRestTemplate.delete(url);
    }
}
