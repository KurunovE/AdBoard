package com.solarlab.adboard.controller;

import com.solarlab.adboard.config.SecurityUtils;
import com.solarlab.adboard.dto.request.user.UpdateUserRequest;
import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserController userController;

    @Test
    void userControllerShouldDelegate() {
        UserResponse response = UserResponse.builder().id(1L).email("user@test.com").build();
        when(userService.findUserById(1L)).thenReturn(response);
        when(userService.updateUser(1L, new UpdateUserRequest("User", "+123")))
                .thenReturn(response);
        assertEquals(response, userController.getUser(1L).getBody());
        assertEquals(response, userController.updateUser(
                1L, new UpdateUserRequest("User", "+123")).getBody()
        );
        assertEquals(HttpStatus.NO_CONTENT, userController.deleteUser(1L).getStatusCode());
    }
}
