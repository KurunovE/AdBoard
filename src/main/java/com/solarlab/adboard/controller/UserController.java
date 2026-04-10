package com.solarlab.adboard.controller;

import com.solarlab.adboard.dto.request.user.UpdateUserRequest;
import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
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
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PositiveOrZero @PathVariable(name = "id") Long id,
            @RequestBody UpdateUserRequest updateUserRequest
    ) {
        return ResponseEntity.ok(userService.updateUser(id, updateUserRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteUser(
            @PositiveOrZero @PathVariable(name = "id") Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
