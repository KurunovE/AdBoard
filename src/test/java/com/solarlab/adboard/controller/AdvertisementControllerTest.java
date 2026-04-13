package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.advertisement.AdvertisementCreateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementStatusUpdateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementUpdateRequest;
import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.enums.AdvertisementStatus;
import com.solarlab.adboard.service.AdvertisementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertisementControllerTest {

    @Mock
    private AdvertisementService advertisementService;
    @InjectMocks
    private AdvertisementController advertisementController;

    @Test
    void getAdvertisementsShouldReturnOk() {
        AdvertisementResponse response = AdvertisementResponse.builder().id(1L).build();
        when(advertisementService.findAll(any())).thenReturn(List.of(response));

        var result = advertisementController.getAdvertisements(
                1L, 2L, BigDecimal.ONE, BigDecimal.TEN
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getAdvertisementByIdShouldReturnOk() {
        AdvertisementResponse response = AdvertisementResponse.builder().id(1L).build();
        when(advertisementService.findById(1L)).thenReturn(response);

        assertEquals(response, advertisementController.getAdvertisementById(1L).getBody());
    }

    @Test
    void createAdvertisementShouldDelegate() {
        AdvertisementCreateRequest request = new AdvertisementCreateRequest(
                "t", "d", BigDecimal.ONE, 1L
        );
        AdvertisementResponse response = AdvertisementResponse.builder().id(1L).build();
        when(advertisementService.create(request)).thenReturn(response);

        assertEquals(response, advertisementController.createAdvertisement(request).getBody());
    }

    @Test
    void updateAdvertisementShouldDelegate() {
        AdvertisementUpdateRequest request = new AdvertisementUpdateRequest(
                "t", "d", BigDecimal.ONE, 1L
        );
        AdvertisementResponse response = AdvertisementResponse.builder().id(1L).build();
        when(advertisementService.update(1L, request)).thenReturn(response);

        assertEquals(response, advertisementController.updateAdvertisement(1L, request).getBody());
    }

    @Test
    void changeStatusShouldDelegate() {
        AdvertisementStatusUpdateRequest request = new AdvertisementStatusUpdateRequest(
                AdvertisementStatus.CLOSED);
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L)
                .status(AdvertisementStatus.CLOSED)
                .build();
        when(advertisementService.changeStatus(1L, request)).thenReturn(response);

        assertEquals(response,
                advertisementController.changeAdvertisementStatus(1L, request).getBody());
    }

    @Test
    void deleteAdvertisementShouldReturnNoContent() {
        var result = advertisementController.deleteAdvertisement(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(advertisementService).delete(1L);
    }
}
