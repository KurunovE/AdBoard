package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.LoginRequest;
import com.solarlab.adboard.dto.request.UserRequestRegistration;
import com.solarlab.adboard.dto.response.LoginResponse;
import com.solarlab.adboard.dto.response.UserResponseRegistration;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${jwt.auth.converter.resource-id}")
    private String clientId;

    @Transactional
    public UserResponseRegistration registerUser(UserRequestRegistration userRequestRegistration) {

        User newUser = User.builder()
                .name(userRequestRegistration.name())
                .email(userRequestRegistration.email())
                .phone(userRequestRegistration.phone())
                .password(userRequestRegistration.password())
                .build();

        User savedUser = userRepository.save(newUser);

        return userMapper.toUserResponseRegistration(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("username", loginRequest.username());
        map.add("password", loginRequest.password());
        map.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                LoginResponse.class
        );

        return response.getBody();
    }
}
