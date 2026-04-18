package com.solarlab.adboard.config;

import com.solarlab.adboard.dto.request.keycloak.KeycloakLoginRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakLogoutRequest;
import com.solarlab.adboard.dto.request.keycloak.KeycloakRefreshTokenRequest;
import com.solarlab.adboard.exception.YandexDiskException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigAndDtoTest {

    @Test
    void keycloakPropertiesShouldBuildUrls() {
        KeycloakProperties properties = new KeycloakProperties(
                "http://localhost:9090",
                "adboard",
                "client",
                "secret",
                "admin",
                "password",
                "admin-cli"
        );

        assertEquals("http://localhost:9090/realms/adboard/protocol/openid-connect/token", properties.tokenUrl());
        assertEquals("http://localhost:9090/realms/master/protocol/openid-connect/token", properties.adminTokenUrl());
        assertEquals("http://localhost:9090/admin/realms/adboard/users", properties.usersUrl());
        assertEquals("http://localhost:9090/admin/realms/adboard/clients", properties.clientsUrl());
    }

    @Test
    void yandexDiskPropertiesShouldExposeConfiguredValues() {
        YandexDiskProperties properties = new YandexDiskProperties(
                "token",
                "https://cloud-api.yandex.net/v1/disk/resources"
        );

        assertEquals("token", properties.token());
        assertEquals("https://cloud-api.yandex.net/v1/disk/resources", properties.apiUrl());
    }

    @Test
    void mailPropertiesShouldExposeConfiguredValues() {
        MailProperties properties = new MailProperties(
                "smtp.yandex.ru",
                587,
                "mail@yandex.ru",
                "secret",
                "mail@yandex.ru",
                Map.of("mail.smtp.auth", "true", "mail.smtp.starttls.enable", "true")
        );

        assertEquals("smtp.yandex.ru", properties.host());
        assertEquals(587, properties.port());
        assertEquals("mail@yandex.ru", properties.from());
        assertEquals("true", properties.properties().get("mail.smtp.auth"));
    }


    @Test
    void keycloakLoginRequestShouldBuildFormData() {
        KeycloakLoginRequest request = KeycloakLoginRequest.builder()
                .grantType("password")
                .clientId("client")
                .username("user@test.com")
                .password("pass")
                .scope("openid")
                .clientSecret("secret")
                .build();

        var formData = request.toFormData();

        assertEquals("password", formData.getFirst("grant_type"));
        assertEquals("client", formData.getFirst("client_id"));
        assertEquals("secret", formData.getFirst("client_secret"));
    }

    @Test
    void keycloakRefreshTokenRequestShouldBuildFormData() {
        KeycloakRefreshTokenRequest request = KeycloakRefreshTokenRequest.builder()
                .grantType("refresh_token")
                .clientId("client")
                .refreshToken("refresh-1")
                .clientSecret("secret")
                .build();

        var formData = request.toFormData();

        assertEquals("refresh_token", formData.getFirst("grant_type"));
        assertEquals("client", formData.getFirst("client_id"));
        assertEquals("refresh-1", formData.getFirst("refresh_token"));
        assertEquals("secret", formData.getFirst("client_secret"));
    }

    @Test
    void keycloakLogoutRequestShouldBuildFormData() {
        KeycloakLogoutRequest request = KeycloakLogoutRequest.builder()
                .clientId("client")
                .refreshToken("refresh-1")
                .clientSecret("secret")
                .build();

        var formData = request.toFormData();

        assertEquals("client", formData.getFirst("client_id"));
        assertEquals("refresh-1", formData.getFirst("refresh_token"));
        assertEquals("secret", formData.getFirst("client_secret"));
    }

    @Test
    void restTemplateConfigShouldCreateTemplatesAndAttachYandexInterceptor() throws Exception {
        RestTemplateConfig config = new RestTemplateConfig(new YandexDiskProperties(
                "token",
                "https://cloud-api.yandex.net/v1/disk/resources"
        ));

        RestTemplate defaultTemplate = config.restTemplate();
        RestTemplate yandexTemplate = config.yandexRestTemplate();

        assertNotNull(defaultTemplate);
        assertEquals(1, yandexTemplate.getInterceptors().size());

        ClientHttpRequestInterceptor interceptor = yandexTemplate.getInterceptors().getFirst();
        MockClientHttpRequest request = new MockClientHttpRequest();
        ClientHttpRequestExecution execution = (req, body) -> {
            assertEquals("OAuth token", req.getHeaders().getFirst("Authorization"));
            return new org.springframework.mock.http.client.MockClientHttpResponse(new byte[0], org.springframework.http.HttpStatus.OK);
        };
        interceptor.intercept(request, "body".getBytes(StandardCharsets.UTF_8), execution);
    }

    @Test
    void jacksonConfigShouldCreateObjectMapper() {
        assertNotNull(new JacksonConfig().objectMapper());
    }

    @Test
    void jwtAuthConverterShouldConvertAuthorities() {
        JwtAuthConverter converter = new JwtAuthConverter();
        ReflectionTestUtils.setField(converter, "principleAttribute", "preferred_username");
        ReflectionTestUtils.setField(converter, "resourceId", "adboard-client");
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "user@test.com")
                .claim("realm_access", Map.of("roles", List.of("user")))
                .claim("resource_access", Map.of("adboard-client", Map.of("roles", List.of("admin"))))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var token = converter.convert(jwt);

        assertInstanceOf(JwtAuthenticationToken.class, token);
        assertEquals("user@test.com", token.getName());
        var authorities = token.getAuthorities();
        assertEquals(true, authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(true, authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void currentUserProviderShouldReadEmailClaimAndAdminRole() {
        CurrentUserProvider provider = new CurrentUserProvider();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", "user@test.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        try {
            var result = provider.getCurrentUser();

            assertTrue(result.isPresent());
            assertEquals("user@test.com", result.get().email());
            assertTrue(result.get().admin());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void currentUserProviderShouldFallbackToPreferredUsername() {
        CurrentUserProvider provider = new CurrentUserProvider();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "fallback@test.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of())
        );

        try {
            var result = provider.getCurrentUser();

            assertTrue(result.isPresent());
            assertEquals("fallback@test.com", result.get().email());
            assertEquals(false, result.get().admin());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void yandexDiskExceptionShouldExposeFields() {
        YandexDiskException exception = new YandexDiskException("fail", HttpStatusCode.valueOf(500), "body");

        assertEquals("fail", exception.getMessage());
        assertEquals(500, exception.getStatusCode().value());
        assertEquals("body", exception.getResponseBody());
    }
}
