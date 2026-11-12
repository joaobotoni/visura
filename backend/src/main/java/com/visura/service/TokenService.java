package com.visura.service;

import com.visura.database.entity.User;
import com.visura.dto.response.TokenResponse;
import com.visura.mapper.TokenMapper;
import com.visura.token.Claims;
import com.visura.token.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtToken jwtToken;
    private final TokenMapper tokenMapper;

    public TokenResponse issue(User user, Integer session, String refreshToken) {
        String accessToken = sign(user, session);
        return show(accessToken, refreshToken);
    }

    public Claims verify(String token) {
        return jwtToken.verify(token);
    }

    private String sign(User user, Integer session) {
        return jwtToken.issue(user.getUuid(), session);
    }

    private TokenResponse show(String accessToken, String refreshToken) {
        return tokenMapper.response(accessToken, refreshToken, jwtToken.expiration());
    }
}
