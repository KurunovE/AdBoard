package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.comment.CommentRequest;
import com.solarlab.adboard.dto.response.comment.CommentResponse;
import com.solarlab.adboard.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/advertisements/{advertisementId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId
    ) {
        return ResponseEntity.ok(commentService.findAllCommentsByAdId(advertisementId));
    }

    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId,
            @Valid @RequestBody CommentRequest commentRequest
    ) {
        return ResponseEntity.ok(commentService.createComment(advertisementId, commentRequest));
    }

    @DeleteMapping("/{id}/delete")
    public void deleteComment(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        commentService.deleteComment(id);
    }
}
