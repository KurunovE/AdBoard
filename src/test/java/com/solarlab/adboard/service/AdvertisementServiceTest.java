package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.advertisement.AdvertisementCreateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementFilter;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementStatusUpdateRequest;
import com.solarlab.adboard.dto.request.advertisement.AdvertisementUpdateRequest;
import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.enums.AdvertisementStatus;
import com.solarlab.adboard.mapper.AdvertisementMapper;
import com.solarlab.adboard.model.Advertisement;
import com.solarlab.adboard.model.Category;
import com.solarlab.adboard.model.Comment;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.CategoryRepository;
import com.solarlab.adboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository advertisementRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdvertisementMapper advertisementMapper;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private AdvertisementService advertisementService;

    private Category category;
    private User user;
    private Advertisement advertisement;
    private AdvertisementResponse response;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(10L).name("Tech").build();
        user = User.builder().id(5L).email("user@test.com").name("User").phone("+123").build();
        advertisement = Advertisement.builder()
                .id(1L)
                .title("Laptop")
                .description("Gaming")
                .price(BigDecimal.valueOf(100))
                .status(AdvertisementStatus.ACTIVE)
                .author(user)
                .category(category)
                .images(List.of())
                .comments(List.of())
                .build();
        response = AdvertisementResponse.builder().id(1L).title("Laptop").build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAllShouldValidateFilterAndMapResult() {
        when(advertisementRepository.findAllWithFilters(
                1L, 2L, BigDecimal.ONE, BigDecimal.TEN
        ))
                .thenReturn(List.of(advertisement));
        when(advertisementMapper.toAdvertisementResponse(advertisement)).thenReturn(response);

        List<AdvertisementResponse> result = advertisementService.findAll(
                new AdvertisementFilter(1L, 2L, BigDecimal.ONE, BigDecimal.TEN)
        );

        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void findAllShouldThrowWhenMinPriceGreaterThanMaxPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                advertisementService.findAll(
                        new AdvertisementFilter(
                                null, null, BigDecimal.TEN, BigDecimal.ONE
                        )
                )
        );
    }

    @Test
    void findByIdShouldReturnMappedResponse() {
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(advertisementMapper.toAdvertisementResponse(advertisement)).thenReturn(response);

        assertEquals(response, advertisementService.findById(1L));
    }

    @Test
    void findByIdShouldThrowWhenMissing() {
        when(advertisementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> advertisementService.findById(1L));
    }

    @Test
    void createShouldSaveAdvertisementForCurrentUser() {
        mockAuthenticatedUser("user@test.com");
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(advertisementRepository.save(any(Advertisement.class)))
                .thenAnswer(invocation -> {
            Advertisement saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(advertisementMapper.toAdvertisementResponse(any(Advertisement.class)))
                .thenReturn(response);

        AdvertisementResponse result = advertisementService.create(
                new AdvertisementCreateRequest("Laptop", "Gaming", BigDecimal.TEN, 10L)
        );

        assertEquals(response, result);
        verify(advertisementRepository).save(any(Advertisement.class));
    }

    @Test
    void updateShouldChangeOnlyProvidedFields() {
        Category newCategory = Category.builder().id(11L).name("Phones").build();
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(categoryRepository.findById(11L)).thenReturn(Optional.of(newCategory));
        when(advertisementRepository.save(advertisement)).thenReturn(advertisement);
        when(advertisementMapper.toAdvertisementResponse(advertisement)).thenReturn(response);

        AdvertisementResponse result = advertisementService.update(
                1L,
                new AdvertisementUpdateRequest("Phone", "Desc", BigDecimal.valueOf(200), 11L)
        );

        assertEquals(response, result);
        assertEquals("Phone", advertisement.getTitle());
        assertEquals("Desc", advertisement.getDescription());
        assertEquals(BigDecimal.valueOf(200), advertisement.getPrice());
        assertEquals(newCategory, advertisement.getCategory());
    }

    @Test
    void changeStatusShouldReturnWithoutSaveWhenSameStatus() {
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(advertisementMapper.toAdvertisementResponse(advertisement)).thenReturn(response);

        AdvertisementResponse result = advertisementService.changeStatus(
                1L,
                new AdvertisementStatusUpdateRequest(AdvertisementStatus.ACTIVE)
        );

        assertEquals(response, result);
        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void deleteShouldRemoveImagesAndAdvertisement() {
        Image image = Image.builder().id(100L).build();
        advertisement.setImages(List.of(image));
        advertisement.setComments(List.of(Comment.builder().id(7L).build()));
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));

        advertisementService.delete(1L);

        verify(imageService).deleteImage(100L);
        verify(advertisementRepository).deleteById(1L);
    }

    private void mockAuthenticatedUser(String email) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", email)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(jwt, null));
    }
}
