package com.solarlab.adboard.controller;

import com.solarlab.adboard.config.SecurityUtils;
import com.solarlab.adboard.dto.request.comment.CommentRequest;
import com.solarlab.adboard.dto.response.comment.CommentResponse;
import com.solarlab.adboard.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CommentController commentController;

    @Test
    void commentControllerShouldDelegate() {
        CommentResponse response = CommentResponse.builder().id(1L).text("Hi").build();
        when(commentService.findAllCommentsByAdId(1L)).thenReturn(List.of(response));
        when(commentService.createComment(1L, new CommentRequest("Hi")))
                .thenReturn(response);
        assertEquals(1, commentController.getAllComments(1L).getBody().size());
        assertEquals(response, commentController.createComment(1L, new CommentRequest("Hi")).getBody());
        assertEquals(HttpStatus.NO_CONTENT, commentController.deleteComment(1L).getStatusCode());
    }
}
