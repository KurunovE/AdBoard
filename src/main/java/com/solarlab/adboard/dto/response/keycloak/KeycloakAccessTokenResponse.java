package com.solarlab.adboard.dto.response.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakAccessTokenResponse(
        @JsonProperty("access_token") String accessToken
) {
}
