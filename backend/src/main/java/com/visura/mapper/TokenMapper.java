package com.visura.mapper;

import com.visura.dto.response.TokenResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface TokenMapper {

    TokenResponse response(String accessToken, String refreshToken, long expiresIn);
}
