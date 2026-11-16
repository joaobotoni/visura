package com.visura.controller;

import com.visura.dto.request.GoogleLoginRequest;
import com.visura.dto.request.LoginRequest;
import com.visura.dto.request.RecoveryCodeRequest;
import com.visura.dto.request.RecoveryRequest;
import com.visura.dto.request.RefreshRequest;
import com.visura.dto.request.RegisterRequest;
import com.visura.dto.request.ResetPasswordRequest;
import com.visura.dto.response.AuthenticationResponse;
import com.visura.dto.response.TokenResponse;
import com.visura.network.ClientNetwork;
import com.visura.principal.Principal;
import com.visura.service.GoogleLoginService;
import com.visura.service.LoginService;
import com.visura.service.LogoutService;
import com.visura.service.RecoveryService;
import com.visura.service.RefreshService;
import com.visura.service.RegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@RequestMapping(path = "/auth", version = "1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final RefreshService refreshService;
    private final LogoutService logoutService;
    private final GoogleLoginService googleLoginService;
    private final RecoveryService recoveryService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.register(request, ip(http)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(loginService.login(request, ip(http)));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> google(@RequestBody @Valid GoogleLoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(googleLoginService.login(request, ip(http)));
    }

    /** Envia o código de recuperação ao e-mail, se houver conta; responde igual nos dois casos. */
    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgot(@RequestBody @Valid RecoveryRequest request) {
        recoveryService.request(request.email());
        return ResponseEntity.noContent().build();
    }

    /** Confere o código sem trocar a senha (gasta uma tentativa). */
    @PostMapping("/password/verify")
    public ResponseEntity<Void> verify(@RequestBody @Valid RecoveryCodeRequest request) {
        recoveryService.confirm(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    /** Confere o código, grava a nova senha e encerra todas as sessões; o app entra de novo com a senha nova. */
    @PostMapping("/password/reset")
    public ResponseEntity<Void> reset(@RequestBody @Valid ResetPasswordRequest request) {
        recoveryService.reset(request.email(), request.code(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(refreshService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Principal principal) {
        logoutService.logout(principal.user(), principal.session());
        return ResponseEntity.noContent().build();
    }

    private static InetAddress ip(HttpServletRequest http) {
        return ClientNetwork.address(http.getRemoteAddr());
    }
}
