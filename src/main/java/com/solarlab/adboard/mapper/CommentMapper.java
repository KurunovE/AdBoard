package com.solarlab.adboard.mapper;

import com.solarlab.adboard.dto.request.CommentRequest;
import com.solarlab.adboard.dto.response.CommentResponse;
import com.solarlab.adboard.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "advertisementId", source = "advertisement.id")
    CommentResponse toCommentResponse(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "advertisement", ignore = true)
    Comment toEntity(CommentRequest request);
}
