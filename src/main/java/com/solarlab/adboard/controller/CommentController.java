package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.comment.CommentRequest;
import com.solarlab.adboard.dto.response.ExceptionResponse;
import com.solarlab.adboard.dto.response.comment.CommentResponse;
import com.solarlab.adboard.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/advertisements/{advertisementId}/comments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Comments", description = "Operations with advertisement comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Get comments for advertisement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments returned"),
            @ApiResponse(responseCode = "400", description = "Invalid id",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId
    ) {
        return ResponseEntity.ok(commentService.findAllCommentsByAdId(advertisementId));
    }

    @Operation(summary = "Create comment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment created"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advertisement not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentResponse> createComment(
            @PositiveOrZero @PathVariable(name = "advertisementId") Long advertisementId,
            @Valid @RequestBody CommentRequest commentRequest
    ) {
        return ResponseEntity.ok(commentService.createComment(advertisementId, commentRequest));
    }

    @Operation(summary = "Delete comment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid id",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PreAuthorize("@securityUtils.isCommentOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
