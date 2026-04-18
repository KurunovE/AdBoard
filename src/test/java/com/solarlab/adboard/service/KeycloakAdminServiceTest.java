package com.solarlab.adboard.service;

import com.solarlab.adboard.config.KeycloakProperties;
import com.solarlab.adboard.dto.request.keycloak.KeycloakUserCreateRequest;
import com.solarlab.adboard.dto.response.keycloak.KeycloakAccessTokenResponse;
import com.solarlab.adboard.dto.response.keycloak.KeycloakClientResponse;
import com.solarlab.adboard.dto.response.keycloak.KeycloakRoleResponse;
import com.solarlab.adboard.dto.response.keycloak.KeycloakUserSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakAdminServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private KeycloakProperties keycloakProperties;

    @InjectMocks
    private KeycloakAdminService keycloakAdminService;

    @BeforeEach
    void setUp() {
        lenient().when(keycloakProperties.adminTokenUrl()).thenReturn("http://keycloak/admin/token");
        lenient().when(keycloakProperties.usersUrl()).thenReturn("http://keycloak/users");
        lenient().when(keycloakProperties.clientsUrl()).thenReturn("http://keycloak/clients");
        lenient().when(keycloakProperties.clientId()).thenReturn("adboard-client");
        lenient().when(keycloakProperties.adminClientId()).thenReturn("admin-cli");
        lenient().when(keycloakProperties.adminUsername()).thenReturn("admin");
        lenient().when(keycloakProperties.adminPassword()).thenReturn("secret");
    }

    @Test
    void createUserShouldReturnIdFromLocationHeader() {
        mockAdminToken();
        when(restTemplate.postForEntity(eq("http://keycloak/users"), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.created(URI.create("http://keycloak/users/kc-1")).build());

        String userId = keycloakAdminService.createUser(createRequest());

        assertEquals("kc-1", userId);
    }

    @Test
    void createUserShouldMapConflictToIllegalArgument() {
        mockAdminToken();
        when(restTemplate.postForEntity(eq("http://keycloak/users"), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.CONFLICT, "Conflict", null, new byte[0], null
                ));

        assertThrows(IllegalArgumentException.class, () -> keycloakAdminService.createUser(createRequest()));
    }

    @Test
    void createUserShouldThrowWhenLocationMissing() {
        mockAdminToken();
        when(restTemplate.postForEntity(eq("http://keycloak/users"), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        assertThrows(IllegalStateException.class, () -> keycloakAdminService.createUser(createRequest()));
    }

    @Test
    void assignClientRoleToUserShouldPostRoleMapping() {
        mockAdminToken();
        when(restTemplate.exchange(
                eq("http://keycloak/clients?clientId=adboard-client"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakClientResponse[].class)
        )).thenReturn(ResponseEntity.ok(new KeycloakClientResponse[]{
                new KeycloakClientResponse("client-uuid", "adboard-client")
        }));
        when(restTemplate.exchange(
                eq("http://keycloak/clients/client-uuid/roles/USER"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakRoleResponse.class)
        )).thenReturn(ResponseEntity.ok(new KeycloakRoleResponse(
                "role-1", "USER", false, true, "client-uuid"
        )));
        when(restTemplate.postForEntity(
                eq("http://keycloak/users/kc-1/role-mappings/clients/client-uuid"),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.ok().build());

        keycloakAdminService.assignClientRoleToUser("kc-1", "USER");

        verify(restTemplate).postForEntity(
                eq("http://keycloak/users/kc-1/role-mappings/clients/client-uuid"),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void assignClientRoleToUserShouldFailWhenClientMissing() {
        mockAdminToken();
        when(restTemplate.exchange(
                eq("http://keycloak/clients?clientId=adboard-client"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakClientResponse[].class)
        )).thenReturn(ResponseEntity.ok(new KeycloakClientResponse[0]));

        assertThrows(IllegalStateException.class, () ->
                keycloakAdminService.assignClientRoleToUser("kc-1", "USER")
        );
    }

    @Test
    void findUserIdByEmailShouldMatchIgnoringCase() {
        mockAdminToken();
        when(restTemplate.exchange(
                contains("email=user@test.com"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakUserSummaryResponse[].class)
        )).thenReturn(ResponseEntity.ok(new KeycloakUserSummaryResponse[]{
                new KeycloakUserSummaryResponse("kc-1", "user", "USER@TEST.COM"),
                new KeycloakUserSummaryResponse("kc-2", "another", "another@test.com")
        }));

        String userId = keycloakAdminService.findUserIdByEmail("user@test.com");

        assertEquals("kc-1", userId);
    }

    @Test
    void deleteUserByEmailShouldSkipWhenUserIsMissing() {
        mockAdminToken();
        when(restTemplate.exchange(
                contains("email=missing@test.com"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakUserSummaryResponse[].class)
        )).thenReturn(ResponseEntity.ok(new KeycloakUserSummaryResponse[0]));

        keycloakAdminService.deleteUserByEmail("missing@test.com");

        verify(restTemplate, never()).exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void updateUserEmailShouldThrowWhenUserIsMissing() {
        mockAdminToken();
        when(restTemplate.exchange(
                contains("email=old@test.com"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakUserSummaryResponse[].class)
        )).thenReturn(ResponseEntity.ok(new KeycloakUserSummaryResponse[0]));

        assertThrows(IllegalStateException.class, () ->
                keycloakAdminService.updateUserEmail("old@test.com", "new@test.com")
        );
    }

    @Test
    void updateUserEmailShouldMapConflictToIllegalArgument() {
        mockAdminToken();
        when(restTemplate.exchange(
                contains("email=old@test.com"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KeycloakUserSummaryResponse[].class)
        )).thenReturn(ResponseEntity.ok(new KeycloakUserSummaryResponse[]{
                new KeycloakUserSummaryResponse("kc-1", "user", "old@test.com")
        }));
        when(restTemplate.exchange(
                eq("http://keycloak/users/kc-1"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", null, new byte[0], null
        ));

        assertThrows(IllegalArgumentException.class, () ->
                keycloakAdminService.updateUserEmail("old@test.com", "new@test.com")
        );
    }

    @Test
    void deleteUserByIdShouldWrapRemoteError() {
        mockAdminToken();
        when(restTemplate.exchange(
                eq("http://keycloak/users/kc-1"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, "failed".getBytes(), null
        ));

        assertThrows(IllegalStateException.class, () -> keycloakAdminService.deleteUserById("kc-1"));
    }

    private void mockAdminToken() {
        when(restTemplate.postForEntity(
                eq("http://keycloak/admin/token"),
                any(HttpEntity.class),
                eq(KeycloakAccessTokenResponse.class)
        )).thenReturn(ResponseEntity.ok(new KeycloakAccessTokenResponse("admin-token")));
    }

    private KeycloakUserCreateRequest createRequest() {
        return new KeycloakUserCreateRequest(
                "user@test.com",
                "user@test.com",
                true,
                true,
                List.of(),
                List.of()
        );
    }
}
