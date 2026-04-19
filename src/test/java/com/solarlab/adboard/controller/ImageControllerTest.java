package com.solarlab.adboard.controller;

import com.solarlab.adboard.config.SecurityUtils;
import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.mapper.ImageMapper;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;
    @Mock
    private ImageMapper imageMapper;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ImageController imageController;

    @Test
    void getAdvertisementImagesShouldReturnMappedList() {
        Image firstImage = Image.builder().id(1L).build();
        Image secondImage = Image.builder().id(2L).build();
        ImageResponse firstResponse = ImageResponse.builder().id(1L).build();
        ImageResponse secondResponse = ImageResponse.builder().id(2L).build();
        when(imageService.getAdvertisementImages(1L)).thenReturn(List.of(firstImage, secondImage));
        when(imageMapper.toImageResponses(List.of(firstImage, secondImage)))
                .thenReturn(List.of(firstResponse, secondResponse));

        assertEquals(
                List.of(firstResponse, secondResponse),
                imageController.getAdvertisementImages(1L).getBody()
        );
    }

    @Test
    void imageControllerShouldDelegateUploadAndDelete() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());
        Image image = Image.builder().id(1L).build();
        ImageResponse response = ImageResponse.builder().id(1L).build();
        when(imageService.uploadImage(file, 1L)).thenReturn(image);
        when(imageMapper.toImageResponse(image)).thenReturn(response);

        assertEquals(response, imageController.uploadImage(1L, file).getBody());
        assertEquals(HttpStatus.NO_CONTENT, imageController.deleteImage(1L).getStatusCode());
        verify(imageService).deleteImage(1L);
    }
}
