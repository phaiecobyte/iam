package com.phaiecobyte.iam.mapper;

import com.phaiecobyte.iam.dto.req.UserReq;
import com.phaiecobyte.iam.dto.res.UserRes;
import com.phaiecobyte.iam.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "emailNormalized", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerifiedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toEntity(UserReq req);

    UserRes toRes(User user);
}
