package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.CommentRequest;
import com.solarlab.adboard.dto.response.CommentResponse;
import com.solarlab.adboard.mapper.CommentMapper;
import com.solarlab.adboard.model.Advertisement;
import com.solarlab.adboard.model.Comment;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.CommentRepository;
import com.solarlab.adboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public List<CommentResponse> findAllCommentsByAdId(Long advertisementId) {
        return commentRepository.findAllByAdvertisementId(advertisementId).stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long advertisementId, CommentRequest commentRequest) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement whith id " + advertisementId + " not found"
                ));

        User author = getCurrentUser();

        Comment comment = commentMapper.toEntity(commentRequest);
        comment.setAdvertisement(advertisement);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentResponse(savedComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id " + id + " not found");
        }
        commentRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }
        final String finalEmail = email;
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email " + finalEmail + " not found"
                ));
    }
}
