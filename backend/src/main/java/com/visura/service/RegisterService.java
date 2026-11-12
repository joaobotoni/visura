package com.visura.service;

import com.visura.dto.request.RegisterRequest;
import com.visura.dto.response.AuthenticationResponse;
import com.visura.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final LocalCredentialService localCredentialService;
    private final AccountService accountService;

    public AuthenticationResponse register(RegisterRequest request, InetAddress ip) {
        PasswordHash hash = hash(request);
        return open(request, hash, ip);
    }

    private PasswordHash hash(RegisterRequest request) {
        return localCredentialService.hash(request.password().password());
    }

    private AuthenticationResponse open(RegisterRequest request, PasswordHash hash, InetAddress ip) {
        return accountService.open(request, hash, ip);
    }
}
