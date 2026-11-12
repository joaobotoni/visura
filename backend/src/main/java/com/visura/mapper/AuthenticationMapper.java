package com.visura.mapper;

import com.visura.database.entity.User;
import com.visura.dto.response.AuthenticationResponse;
import com.visura.dto.response.TokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface AuthenticationMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "token", source = "token")
    AuthenticationResponse response(User user, TokenResponse token);
}
