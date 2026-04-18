package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.user.UpdateUserRequest;
import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @InjectMocks
    private UserService userService;

    @Test
    void findUserByIdShouldReturnMappedUser() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("User")
                .phone("+123")
                .build();
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("user@test.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(response);

        assertEquals(response, userService.findUserById(1L));
    }

    @Test
    void updateUserShouldSkipSaveWhenNothingChanged() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("User")
                .phone("+123")
                .build();
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("user@test.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(response);

        assertEquals(response, userService.updateUser(1L, new UpdateUserRequest("User", "+123")));
        verify(userRepository, never()).save(user);
    }

    @Test
    void deleteUserShouldDeleteFromKeycloakAndRepository() {
        User user = User.builder().id(1L).email("user@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(keycloakAdminService).deleteUserByEmail("user@test.com");
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldThrowWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(1L));
    }
}
