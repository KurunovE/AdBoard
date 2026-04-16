package com.solarlab.adboard.config;

import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.AdvertisementRepository;
import com.solarlab.adboard.repository.CommentRepository;
import com.solarlab.adboard.repository.ImageRepository;
import com.solarlab.adboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Function;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;

    public boolean isOwner(Long userId) {
        return checkOwnership(current -> userRepository.findById(userId)
                .map(User::getEmail)
                .map(email -> Objects.equals(email, current.email()))
                .orElse(false));
    }

    public boolean isAdvertisementOwner(Long advertisementId) {
        return checkOwnership(current -> advertisementRepository.findById(advertisementId)
                .map(advertisement -> Objects.equals(
                        advertisement.getAuthor().getEmail(),
                        current.email()
                ))
                .orElse(false));
    }

    public boolean isCommentOwner(Long commentId) {
        return checkOwnership(current -> commentRepository.findById(commentId)
                .map(comment -> Objects.equals(
                        comment.getAuthor().getEmail(),
                        current.email()
                ))
                .orElse(false));
    }

    public boolean isImageOwner(Long imageId) {
        return checkOwnership(current -> imageRepository.findById(imageId)
                .map(image -> Objects.equals(
                        image.getAdvertisement().getAuthor().getEmail(),
                        current.email()
                ))
                .orElse(false));
    }

    private boolean checkOwnership(Function<CurrentUserContext, Boolean> rule) {
        return currentUserProvider.getCurrentUser()
                .map(current -> current.admin() || rule.apply(current))
                .orElse(false);
    }
}
