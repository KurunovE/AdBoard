package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.response.image.ImageResponse;
import com.solarlab.adboard.model.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageResponse toImageResponse(Image image);
}
