package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.UpdateUserRequest;
import com.solarlab.adboard.dto.response.UserResponse;
import com.solarlab.adboard.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PreAuthorize("@securityUtils.isOwner(#id)")
    @PostMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PositiveOrZero @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdateUserRequest updateUserRequest
    ) {
        return ResponseEntity.ok(userService.updateUser(id, updateUserRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public void deleteUser(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        userService.deleteUser(id);
    }
}
