package com.solarlab.adboard.dto.response.keycloak;

public record KeycloakUserSummaryResponse(
        String id,
        String username,
        String email
) {
}
