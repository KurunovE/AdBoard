package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.response.advertisement.AdvertisementResponse;
import com.solarlab.adboard.model.Advertisement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ImageMapper.class})
public interface AdvertisementMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    AdvertisementResponse toAdvertisementResponse(Advertisement advertisement);
}
