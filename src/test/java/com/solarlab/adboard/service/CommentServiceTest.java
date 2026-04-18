package com.solarlab.adboard.service;

import com.solarlab.adboard.config.CurrentUserContext;
import com.solarlab.adboard.config.CurrentUserProvider;
import com.solarlab.adboard.dto.request.comment.CommentRequest;
import com.solarlab.adboard.dto.response.comment.CommentResponse;
import com.solarlab.adboard.mapper.CommentMapper;
import com.solarlab.adboard.model.Advertisement;
import com.solarlab.adboard.model.Comment;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.CommentRepository;
import com.solarlab.adboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private CommentRepository commentRepository;
    @Mock private AdvertisementRepository advertisementRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void findAllCommentsByAdIdShouldThrowWhenAdvertisementMissing() {
        when(advertisementRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> commentService.findAllCommentsByAdId(1L));
    }

    @Test
    void findAllCommentsByAdIdShouldMapComments() {
        Comment comment = Comment.builder()
                .id(1L)
                .text("Hi")
                .build();
        CommentResponse response = CommentResponse.builder()
                .id(1L)
                .text("Hi")
                .build();
        when(advertisementRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAllByAdvertisementId(1L)).thenReturn(List.of(comment));
        when(commentMapper.toCommentResponse(comment)).thenReturn(response);

        assertEquals(List.of(response), commentService.findAllCommentsByAdId(1L));
    }

    @Test
    void createCommentShouldSaveWithCurrentUser() {
        Advertisement advertisement = Advertisement.builder()
                .id(1L)
                .build();
        User user = User.builder()
                .id(5L)
                .email("user@test.com")
                .build();
        Comment comment = Comment.builder()
                .text("Hello")
                .build();
        Comment savedComment = Comment.builder()
                .id(10L)
                .text("Hello")
                .build();
        CommentResponse response = CommentResponse.builder()
                .id(10L)
                .text("Hello")
                .build();
        when(currentUserProvider.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUserContext("user@test.com", false)));
        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(advertisement));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(commentMapper.toEntity(any(CommentRequest.class))).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(commentMapper.toCommentResponse(savedComment)).thenReturn(response);

        assertEquals(response, commentService.createComment(1L, new CommentRequest("Hello")));
        assertEquals(user, comment.getAuthor());
        assertEquals(advertisement, comment.getAdvertisement());
    }

    @Test
    void deleteCommentShouldDeleteWhenExists() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }
}
