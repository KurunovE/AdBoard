package com.solarlab.adboard.service;

import com.solarlab.adboard.config.YandexDiskProperties;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.ImageRepository;
import com.solarlab.adboard.service.impl.YandexDiskImageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private RestTemplate yandexRestTemplate;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private AdvertisementRepository advertisementRepository;

    @Test
    void getAdvertisementImagesShouldThrowWhenAdvertisementMissing() {
        when(advertisementRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> createService().getAdvertisementImages(1L));
    }

    @Test
    void getAdvertisementImagesShouldReturnAllImagesForAdvertisement() {
        Image firstImage = Image.builder().id(1L).sortOrder(0).build();
        Image secondImage = Image.builder().id(2L).sortOrder(1).build();
        when(advertisementRepository.existsById(1L)).thenReturn(true);
        when(imageRepository.findAllByAdvertisementIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(firstImage, secondImage));

        assertEquals(List.of(firstImage, secondImage), createService().getAdvertisementImages(1L));
    }

    @Test
    void getAdvertisementImagesShouldReturnEmptyListWhenAdvertisementHasNoImages() {
        when(advertisementRepository.existsById(1L)).thenReturn(true);
        when(imageRepository.findAllByAdvertisementIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        assertEquals(List.of(), createService().getAdvertisementImages(1L));
    }

    private YandexDiskImageService createService() {
        return new YandexDiskImageService(
                yandexRestTemplate,
                imageRepository,
                advertisementRepository,
                new YandexDiskProperties(
                        "token",
                        "https://cloud-api.yandex.net/v1/disk/resources"
                )
        );
    }
}
