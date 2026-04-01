package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.UpdateUserRequest;
import com.solarlab.adboard.dto.request.UserTestRequest;
import com.solarlab.adboard.dto.response.UserResponse;
import com.solarlab.adboard.dto.response.UserTestResponse;
import com.solarlab.adboard.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/test")
    public ResponseEntity<UserTestResponse> testEndpoint(
            @Valid @RequestBody UserTestRequest userTestRequest
    ) {
        return ResponseEntity.ok(userService.getUserTestResponse(userTestRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable(name = "id") @PositiveOrZero Long id
    ) {
        return ResponseEntity.ok(userService.findUser(id));
    }

    @PostMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable(name = "id") @PositiveOrZero Long id,
            @Valid @RequestBody UpdateUserRequest updateUserRequest
    ) {
        return ResponseEntity.ok(userService.updateUser(id, updateUserRequest));
    }
}
