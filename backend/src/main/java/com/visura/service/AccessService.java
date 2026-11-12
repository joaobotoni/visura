package com.visura.service;

import com.visura.database.entity.Device;
import com.visura.database.entity.Session;
import com.visura.database.entity.User;
import com.visura.dto.request.DeviceRequest;
import com.visura.dto.response.AuthenticationResponse;
import com.visura.dto.response.TokenResponse;
import com.visura.mapper.AuthenticationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;
    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public AuthenticationResponse grant(User user, DeviceRequest request, InetAddress ip) {
        Device device = enroll(user, request);
        Session session = open(device, ip);
        String refreshToken = issue(session);
        TokenResponse tokens = tokens(user, session, refreshToken);
        return show(user, tokens);
    }

    @Transactional
    public TokenResponse rotate(Device device, InetAddress ip) {
        Session session = open(device, ip);
        String refreshToken = issue(session);
        return tokens(device.getUser(), session, refreshToken);
    }

    private Device enroll(User user, DeviceRequest request) {
        return deviceService.enroll(user, request);
    }

    private Session open(Device device, InetAddress ip) {
        return sessionService.open(device, ip);
    }

    private String issue(Session session) {
        return refreshTokenService.issue(session);
    }

    private TokenResponse tokens(User user, Session session, String refreshToken) {
        return tokenService.issue(user, session.getId(), refreshToken);
    }

    private AuthenticationResponse show(User user, TokenResponse tokens) {
        return authenticationMapper.response(user, tokens);
    }
}