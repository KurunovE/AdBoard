package com.solarlab.adboard.service;

import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.keycloak.KeycloakRoleMappingRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakUserCreateRequest;
import com.solarlab.adboard.dto.response.keycloak.KeycloakAccessTokenResponse;
import com.solarlab.adboard.dto.response.keycloak.KeycloakClientResponse;
import com.solarlab.adboard.dto.response.keycloak.KeycloakRoleResponse;
import com.solarlab.adboard.dto.response.keycloak.KeycloakUserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final RestTemplate restTemplate;
    private final KeycloakProperties keycloakProperties;

    public String createUser(KeycloakUserCreateRequest request) {
        String adminAccessToken = getAdminAccessToken();
        HttpHeaders headers = authorizedJsonHeaders(adminAccessToken);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    keycloakProperties.usersUrl(),
                    new HttpEntity<>(request, headers),
                    Void.class
            );

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new IllegalStateException(
                        "Keycloak user creation returned status "
                                + response.getStatusCode().value()
                );
            }

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new IllegalStateException("Keycloak did not return created user location");
            }

            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new IllegalArgumentException("User already exists in Keycloak");
            }
            throw new IllegalStateException(
                    "Failed to create user in Keycloak: " + ex.getResponseBodyAsString(), ex
            );
        }
    }

    public void assignClientRoleToUser(String keycloakUserId, String roleName) {
        String adminAccessToken = getAdminAccessToken();
        String clientUuid = getClientUuid(adminAccessToken);
        KeycloakRoleResponse role = getClientRoleRepresentation(
                clientUuid, roleName, adminAccessToken
        );

        try {
            restTemplate.postForEntity(
                    keycloakProperties.usersUrl()
                            + "/" + keycloakUserId + "/role-mappings/clients/" + clientUuid,
                    new HttpEntity<>(List.of(new KeycloakRoleMappingRequest(
                            role.id(),
                            role.name(),
                            role.composite(),
                            role.clientRole(),
                            role.containerId()
                    )), authorizedJsonHeaders(adminAccessToken)),
                    Void.class
            );
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Failed to assign Keycloak role "
                            + roleName + ": "
                            + ex.getResponseBodyAsString(), ex
            );
        }
    }

    public void deleteUserById(String keycloakUserId) {
        String adminAccessToken = getAdminAccessToken();

        try {
            restTemplate.exchange(
                    keycloakProperties.usersUrl() + "/" + keycloakUserId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(authorizedHeaders(adminAccessToken)),
                    Void.class
            );
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Failed to delete user from Keycloak: " + ex.getResponseBodyAsString(), ex
            );
        }
    }

    public void deleteUserByEmail(String email) {
        String keycloakUserId = findUserIdByEmail(email);

        if (keycloakUserId == null) {
            return;
        }

        deleteUserById(keycloakUserId);
    }

    public String findUserIdByEmail(String email) {
        String adminAccessToken = getAdminAccessToken();

        try {
            ResponseEntity<KeycloakUserSummaryResponse[]> response = restTemplate.exchange(
                    keycloakProperties.usersUrl() + "?email=" + email + "&exact=true",
                    HttpMethod.GET,
                    new HttpEntity<>(authorizedHeaders(adminAccessToken)),
                    KeycloakUserSummaryResponse[].class
            );

            KeycloakUserSummaryResponse[] users = response.getBody();
            if (users == null) {
                return null;
            }

            return Arrays.stream(users)
                    .filter(user -> email.equalsIgnoreCase(user.email()))
                    .map(KeycloakUserSummaryResponse::id)
                    .findFirst()
                    .orElse(null);
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Failed to find user in Keycloak: "
                            + ex.getResponseBodyAsString(), ex
            );
        }
    }

    private String getClientUuid(String adminAccessToken) {
        try {
            ResponseEntity<KeycloakClientResponse[]> response = restTemplate.exchange(
                    keycloakProperties.clientsUrl()
                            + "?clientId=" + keycloakProperties.clientId(),
                    HttpMethod.GET,
                    new HttpEntity<>(authorizedHeaders(adminAccessToken)),
                    KeycloakClientResponse[].class
            );

            KeycloakClientResponse[] clients = response.getBody();
            if (clients == null || clients.length == 0) {
                throw new IllegalStateException(
                        "Keycloak client " + keycloakProperties.clientId() + " not found"
                );
            }

            return Arrays.stream(clients)
                    .map(KeycloakClientResponse::id)
                    .filter(this::hasText)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Keycloak client response does not contain internal id"
                    ));

        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Failed to load Keycloak client metadata: "
                            + ex.getResponseBodyAsString(), ex
            );
        }
    }

    private KeycloakRoleResponse getClientRoleRepresentation(
            String clientUuid,
            String roleName,
            String adminAccessToken
    ) {
        try {
            ResponseEntity<KeycloakRoleResponse> response = restTemplate.exchange(
                    keycloakProperties.clientsUrl() + "/" + clientUuid + "/roles/" + roleName,
                    HttpMethod.GET,
                    new HttpEntity<>(authorizedHeaders(adminAccessToken)),
                    KeycloakRoleResponse.class
            );

            if (response.getBody() == null) {
                throw new IllegalStateException(
                        "Keycloak role " + roleName
                                + " not found for client " + keycloakProperties.clientId()
                );
            }

            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Failed to load Keycloak role " + roleName + ": "
                            + ex.getResponseBodyAsString(), ex
            );
        }
    }

    private String getAdminAccessToken() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "password");
        requestBody.add("client_id", keycloakProperties.adminClientId());
        requestBody.add("username", keycloakProperties.adminUsername());
        requestBody.add("password", keycloakProperties.adminPassword());

        try {
            ResponseEntity<KeycloakAccessTokenResponse> response = restTemplate.postForEntity(
                    keycloakProperties.adminTokenUrl(),
                    new HttpEntity<>(requestBody, formHeaders()),
                    KeycloakAccessTokenResponse.class
            );

            String accessToken = response.getBody() != null
                    ? response.getBody().accessToken()
                    : null;
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("Keycloak admin token response is empty");
            }
            return accessToken;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(
                    "Failed to obtain Keycloak admin token: "
                            + ex.getResponseBodyAsString(), ex
            );
        }
    }

    private HttpHeaders authorizedHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private HttpHeaders authorizedJsonHeaders(String accessToken) {
        HttpHeaders headers = authorizedHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
