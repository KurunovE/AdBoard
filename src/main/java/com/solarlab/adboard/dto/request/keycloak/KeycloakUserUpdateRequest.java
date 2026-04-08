package com.solarlab.adboard.dto.request.keycloak;

import java.util.List;

public record KeycloakUserUpdateRequest(
        String username,
        String email,
        boolean enabled,
        boolean emailVerified,
        List<String> requiredActions
) {
}
