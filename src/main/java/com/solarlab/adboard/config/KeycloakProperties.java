package com.solarlab.adboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
        String authServerUrl,
        String realm,
        String clientId,
        String clientSecret,
        String adminUsername,
        String adminPassword,
        String adminClientId
) {

    public String tokenUrl() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public String adminTokenUrl() {
        return authServerUrl + "/realms/master/protocol/openid-connect/token";
    }

    public String usersUrl() {
        return authServerUrl + "/admin/realms/" + realm + "/users";
    }

    public String clientsUrl() {
        return authServerUrl + "/admin/realms/" + realm + "/clients";
    }
}
