package com.visura.service;

import com.visura.database.entity.User;
import com.visura.database.repository.UserRepository;
import com.visura.dto.request.GoogleLoginRequest;
import com.visura.exception.custom.CredentialException;
import com.visura.exception.enums.problem.CredentialProblem;
import com.visura.support.Controle;
import com.visura.token.GoogleToken;
import com.visura.token.GoogleToken.GoogleIdentity;
import com.visura.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Login com Google")
class GoogleLoginServiceTest {

    private static final InetAddress IP = InetAddress.getLoopbackAddress();
    private static final GoogleIdentity IDENTITY = new GoogleIdentity("1234567890", Email.of("ana@vsr.com"));

    private final GoogleToken googleToken = mock(GoogleToken.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = mock(UserService.class);
    private final AccessService accessService = mock(AccessService.class);
    private final GoogleLoginService googleLoginService =
            new GoogleLoginService(googleToken, userRepository, userService, accessService);

    @Test
    @Controle
    @DisplayName("sem client ID configurado, o login com Google responde 503")
    void unconfiguredIsUnavailable() {
        GoogleToken unconfigured = new GoogleToken("");

        assertThatThrownBy(() -> unconfigured.verify("token"))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.GOOGLE_UNAVAILABLE);
    }

    @Test
    @Controle
    @DisplayName("conta já vinculada entra sem vincular de novo")
    void linkedAccountLogsIn() {
        UUID user = UUID.randomUUID();
        when(googleToken.verify("token")).thenReturn(IDENTITY);
        when(userRepository.findGoogle(IDENTITY.subject())).thenReturn(Optional.of(user));

        googleLoginService.login(new GoogleLoginRequest("token", null), IP);

        verify(userService).find(user);
        verify(userRepository, never()).linkGoogle(anyInt(), anyString());
    }

    @Test
    @Controle
    @DisplayName("no primeiro acesso, a conta com o mesmo e-mail é vinculada")
    void firstAccessLinksByEmail() {
        User user = mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(user.getId()).thenReturn(7);
        when(user.getUuid()).thenReturn(uuid);
        when(googleToken.verify("token")).thenReturn(IDENTITY);
        when(userRepository.findGoogle(IDENTITY.subject())).thenReturn(Optional.empty());
        when(userRepository.findWithPersonByEmail(IDENTITY.email())).thenReturn(Optional.of(user));

        googleLoginService.login(new GoogleLoginRequest("token", null), IP);

        verify(userRepository).linkGoogle(7, IDENTITY.subject());
        verify(userService).find(uuid);
    }

    @Test
    @Controle
    @DisplayName("sem conta com o e-mail, responde 404 e não abre sessão")
    void unknownAccountIsNotFound() {
        when(googleToken.verify("token")).thenReturn(IDENTITY);
        when(userRepository.findGoogle(IDENTITY.subject())).thenReturn(Optional.empty());
        when(userRepository.findWithPersonByEmail(IDENTITY.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> googleLoginService.login(new GoogleLoginRequest("token", null), IP))
                .isInstanceOf(CredentialException.class)
                .extracting("problem").isEqualTo(CredentialProblem.GOOGLE_ACCOUNT_NOT_FOUND);
        verify(accessService, never()).grant(any(), any(), any());
    }
}
