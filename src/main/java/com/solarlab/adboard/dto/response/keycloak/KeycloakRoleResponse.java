package com.solarlab.adboard.dto.response.keycloak;

public record KeycloakRoleResponse(
        String id,
        String name,
        boolean composite,
        boolean clientRole,
        String containerId
) {
}
