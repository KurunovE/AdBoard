package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.dto.response.user.UserResponseRegistration;
import com.solarlab.adboard.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    UserResponseRegistration toUserResponseRegistration(User user);
}
