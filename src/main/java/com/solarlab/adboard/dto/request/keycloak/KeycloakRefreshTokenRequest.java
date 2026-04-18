package com.solarlab.adboard.dto.request.keycloak;

import lombok.Builder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Builder
public record KeycloakRefreshTokenRequest(
        String grantType,
        String clientId,
        String refreshToken,
        String clientSecret
) {

    public MultiValueMap<String, String> toFormData() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", grantType);
        formData.add("client_id", clientId);
        formData.add("refresh_token", refreshToken);

        if (clientSecret != null && !clientSecret.isBlank()) {
            formData.add("client_secret", clientSecret);
        }

        return formData;
    }
}
