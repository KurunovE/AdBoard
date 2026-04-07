package com.solarlab.adboard.dto.request.keycloak;

public record KeycloakCredentialRequest(
        String type,
        String value,
        boolean temporary
) {
}
