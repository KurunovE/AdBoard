package com.solarlab.adboard.dto.request.keycloak;

import lombok.Builder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Builder
public record KeycloakLoginRequest(
        String grantType,
        String clientId,
        String username,
        String password,
        String scope,
        String clientSecret
) {

    public MultiValueMap<String, String> toFormData() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", grantType);
        formData.add("client_id", clientId);
        formData.add("username", username);
        formData.add("password", password);
        formData.add("scope", scope);

        if (clientSecret != null && !clientSecret.isBlank()) {
            formData.add("client_secret", clientSecret);
        }

        return formData;
    }
}
