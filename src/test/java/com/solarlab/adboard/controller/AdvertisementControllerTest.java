package com.solarlab.adboard.controller;

import com.solarlab.adboard.config.JwtAuthConverter;
import com.solarlab.adboard.config.SecurityConfig;
import com.solarlab.adboard.config.SecurityUtils;
import com.solarlab.adboard.service.AdvertisementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvertisementController.class)
@Import(SecurityConfig.class)
class AdvertisementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdvertisementService advertisementService;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private JwtAuthConverter jwtAuthConverter;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void createAdvertisementWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/advertisements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Laptop",
                                  "description": "Gaming",
                                  "price": 10,
                                  "categoryId": 1
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(advertisementService, never()).create(any());
    }

    @Test
    void updateAdvertisementByNonOwnerShouldReturnForbidden() throws Exception {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).ensureAdvertisementOwner(1L);

        mockMvc.perform(put("/v1/advertisements/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "price": 100.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        verify(advertisementService, never()).update(any(), any());
    }

    @Test
    void createAdvertisementWithInvalidBodyShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/advertisements")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Gaming",
                                  "price": null,
                                  "categoryId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Field '")));

        verify(advertisementService, never()).create(any());
    }

    @Test
    void getAdvertisementsWithInvalidPriceRangeShouldReturnBadRequest() throws Exception {
        when(advertisementService.findAll(any()))
                .thenThrow(new IllegalArgumentException("minPrice cannot be greater than maxPrice"));

        mockMvc.perform(get("/v1/advertisements")
                        .param("minPrice", "10")
                        .param("maxPrice", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minPrice cannot be greater than maxPrice"));
    }

    @Test
    void deleteAdvertisementByOwnerShouldReturnNoContent() throws Exception {
        doNothing().when(securityUtils).ensureAdvertisementOwner(1L);
        doNothing().when(advertisementService).delete(1L);

        mockMvc.perform(delete("/v1/advertisements/1")
                        .with(jwt()))
                .andExpect(status().isNoContent());

        verify(advertisementService).delete(1L);
    }

    @Test
    void getAdvertisementByIdWhenMissingShouldReturnNotFound() throws Exception {
        when(advertisementService.findById(999L))
                .thenThrow(new EntityNotFoundException("Advertisement with id 999 not found"));

        mockMvc.perform(get("/v1/advertisements/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Advertisement with id 999 not found"));
    }
}
