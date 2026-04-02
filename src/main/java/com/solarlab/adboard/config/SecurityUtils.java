package com.solarlab.adboard.config;

import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public boolean isOwner(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentEmail = jwt.getClaimAsString("email");
        
        if (currentEmail == null) {
            currentEmail = jwt.getClaimAsString("preferred_username");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return true;

        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().getEmail().equals(currentEmail);
    }
}
