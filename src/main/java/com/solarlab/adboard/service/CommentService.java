package com.solarlab.adboard.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CurrentUserProvider currentUserProvider;
    private final CommentRepository commentRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public List<CommentResponse> findAllCommentsByAdId(Long advertisementId) {
        if (!advertisementRepository.existsById(advertisementId)) {
            throw new EntityNotFoundException(
                    "Advertisement with id " + advertisementId + " not found"
            );
        }

        return commentRepository.findAllByAdvertisementId(advertisementId).stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long advertisementId, CommentRequest commentRequest) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Advertisement with id " + advertisementId + " not found"
                ));

        User author = getCurrentUser();

        Comment comment = commentMapper.toEntity(commentRequest);
        comment.setAdvertisement(advertisement);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);
        log.info("Created comment id={} advertisementId={} authorEmail={}",
                savedComment.getId(), advertisementId, author.getEmail());
        return commentMapper.toCommentResponse(savedComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id " + id + " not found");
        }
        commentRepository.deleteById(id);
        log.info("Deleted comment id={}", id);
    }

    private User getCurrentUser() {
        String email = currentUserProvider.getCurrentUser()
                .map(current -> current.email())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email " + email + " not found"
                ));
    }
}
