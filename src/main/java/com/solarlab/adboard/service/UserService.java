package com.solarlab.adboard.service;

import com.solarlab.adboard.dto.request.UpdateUserRequest;
import com.solarlab.adboard.dto.request.UserTestRequest;
import com.solarlab.adboard.dto.response.UserResponse;
import com.solarlab.adboard.dto.response.UserTestResponse;
import com.solarlab.adboard.mapper.UserMapper;
import com.solarlab.adboard.model.User;
import com.solarlab.adboard.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserTestResponse getUserTestResponse(UserTestRequest userTestRequest) {
        return UserTestResponse.builder()
                .name(userTestRequest.name())
                .email(userTestRequest.email())
                .phone(userTestRequest.phone())
                .password(userTestRequest.password())
                .build();
    }

    public UserResponse findUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id " + id + " not found"
                ));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id " + id + " not found"
                ));

        user.setName(updateUserRequest.name());
        user.setEmail(updateUserRequest.email());
        user.setPhone(updateUserRequest.phone());

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }


}
