package com.solarlab.adboard.dto.request.keycloak;

public record KeycloakRoleMappingRequest(
        String id,
        String name,
        boolean composite,
        boolean clientRole,
        String containerId
) {
}
