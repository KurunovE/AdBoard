package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.request.category.CategoryRequest;
import com.solarlab.adboard.dto.request.comment.CommentRequest;
import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.dto.response.category.CategoryResponse;
import com.solarlab.adboard.dto.response.comment.CommentResponse;
import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.dto.response.user.UserAdvertisementResponse;
import com.solarlab.adboard.dto.response.user.UserRegistrationResponse;
import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.enums.AdvertisementStatus;
import com.solarlab.adboard.model.Advertisement;
import com.solarlab.adboard.model.Category;
import com.solarlab.adboard.model.Comment;
import com.solarlab.adboard.model.Image;
import com.solarlab.adboard.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {

    private final UserMapper userMapper = new UserMapperImpl();
    private final CategoryMapper categoryMapper = new CategoryMapperImpl();
    private final ImageMapper imageMapper = new ImageMapperImpl();
    private final CommentMapper commentMapper = createCommentMapper();
    private final AdvertisementMapper advertisementMapper = createAdvertisementMapper();

    @Test
    void userMapperShouldMapAllResponseTypes() {
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("user@test.com")
                .phone("+123")
                .build();

        UserResponse userResponse = userMapper.toUserResponse(user);
        UserRegistrationResponse registrationResponse = userMapper.toUserRegistrationResponse(user);
        UserAdvertisementResponse advertisementResponse = userMapper.toUserAdvertisementResponse(user);

        assertEquals(1L, userResponse.id());
        assertEquals("user@test.com", registrationResponse.email());
        assertEquals("User", advertisementResponse.name());
    }

    @Test
    void categoryMapperShouldMapEntityAndResponse() {
        Category parent = Category.builder().id(10L).name("Parent").build();
        Category category = Category.builder().id(11L).name("Child").parent(parent).build();

        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        Category entity = categoryMapper.toEntity(new CategoryRequest("Created", 10L));
        Category entityWithoutParent = categoryMapper.toEntity(new CategoryRequest("Root", null));

        assertEquals(11L, response.id());
        assertEquals(10L, response.parentId());
        assertEquals("Created", entity.getName());
        assertEquals(10L, entity.getParent().getId());
        assertNull(entityWithoutParent.getParent());
        assertNull(categoryMapper.mapParentById(null));
        assertEquals(77L, categoryMapper.mapParentById(77L).getId());
    }

    @Test
    void commentMapperShouldMapEntityAndResponse() {
        User author = User.builder()
                .id(2L)
                .name("Author")
                .email("author@test.com")
                .phone("+999")
                .build();
        Advertisement advertisement = Advertisement.builder().id(5L).build();
        Comment comment = Comment.builder()
                .id(3L)
                .text("comment")
                .author(author)
                .advertisement(advertisement)
                .createdAt(LocalDateTime.of(2026, 4, 18, 12, 0))
                .build();

        CommentResponse response = commentMapper.toCommentResponse(comment);
        Comment entity = commentMapper.toEntity(new CommentRequest("new text"));

        assertEquals(3L, response.id());
        assertEquals(5L, response.advertisementId());
        assertEquals("author@test.com", response.author().email());
        assertEquals("new text", entity.getText());
        assertNull(entity.getId());
        assertNull(entity.getAuthor());
        assertNull(entity.getAdvertisement());
    }

    @Test
    void imageMapperShouldMapSingleImageAndCollection() {
        Image first = Image.builder()
                .id(1L)
                .url("http://cdn/1")
                .sortOrder(0)
                .uploadedAt(LocalDateTime.of(2026, 4, 18, 10, 0))
                .build();
        Image second = Image.builder()
                .id(2L)
                .url("http://cdn/2")
                .sortOrder(1)
                .uploadedAt(LocalDateTime.of(2026, 4, 18, 11, 0))
                .build();

        ImageResponse response = imageMapper.toImageResponse(first);
        List<ImageResponse> responses = imageMapper.toImageResponses(List.of(first, second));

        assertEquals(1L, response.id());
        assertEquals(2, responses.size());
        assertEquals("http://cdn/2", responses.get(1).url());
    }

    @Test
    void advertisementMapperShouldMapNestedFields() {
        User author = User.builder()
                .id(7L)
                .name("Seller")
                .email("seller@test.com")
                .phone("+777")
                .build();
        Category category = Category.builder().id(9L).name("Tech").build();
        Advertisement advertisement = Advertisement.builder()
                .id(8L)
                .title("Laptop")
                .description("Gaming")
                .price(BigDecimal.valueOf(1000))
                .status(AdvertisementStatus.ACTIVE)
                .author(author)
                .category(category)
                .createdAt(LocalDateTime.of(2026, 4, 18, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 18, 13, 0))
                .build();

        AdvertisementResponse response = advertisementMapper.toAdvertisementResponse(advertisement);

        assertEquals(8L, response.id());
        assertEquals(9L, response.categoryId());
        assertEquals("Tech", response.categoryName());
        assertNotNull(response.author());
        assertEquals("Seller", response.author().name());
    }

    private CommentMapper createCommentMapper() {
        CommentMapperImpl mapper = new CommentMapperImpl();
        ReflectionTestUtils.setField(mapper, "userMapper", userMapper);
        return mapper;
    }

    private AdvertisementMapper createAdvertisementMapper() {
        AdvertisementMapperImpl mapper = new AdvertisementMapperImpl();
        ReflectionTestUtils.setField(mapper, "userMapper", userMapper);
        return mapper;
    }
}
