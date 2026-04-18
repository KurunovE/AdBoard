package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.response.user.UserAdvertisementResponse;
import com.solarlab.adboard.dto.response.user.UserRegistrationResponse;
import com.solarlab.adboard.dto.response.user.UserResponse;
import com.solarlab.adboard.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserAdvertisementResponse toUserAdvertisementResponse(User user);
    UserResponse toUserResponse(User user);
    UserRegistrationResponse toUserRegistrationResponse(User user);
}
