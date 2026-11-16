package com.visura.service;

import com.visura.database.entity.User;
import com.visura.database.repository.UserRepository;
import com.visura.dto.request.GoogleLoginRequest;
import com.visura.dto.response.AuthenticationResponse;
import com.visura.exception.custom.CredentialException;
import com.visura.exception.enums.problem.CredentialProblem;
import com.visura.token.GoogleToken;
import com.visura.token.GoogleToken.GoogleIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * Login com Google para quem já tem conta: a conta é achada pela credencial social já vinculada ou, no primeiro
 * acesso, pelo e-mail confirmado pelo Google, e aí a credencial é vinculada. Sem conta, responde {@code 404}: o
 * cadastro exige CPF e continua no app.
 */
@Service
@RequiredArgsConstructor
public class GoogleLoginService {

    private final GoogleToken googleToken;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AccessService accessService;

    public AuthenticationResponse login(GoogleLoginRequest request, InetAddress ip) {
        GoogleIdentity identity = verify(request.idToken());
        User user = find(account(identity));
        return grant(user, request, ip);
    }

    private GoogleIdentity verify(String idToken) {
        return googleToken.verify(idToken);
    }

    private User find(UUID user) {
        return userService.find(user);
    }

    private AuthenticationResponse grant(User user, GoogleLoginRequest request, InetAddress ip) {
        return accessService.grant(user, request.device(), ip);
    }

    private Optional<UUID> linked(GoogleIdentity identity) {
        return userRepository.findGoogle(identity.subject());
    }

    private Optional<UUID> bind(GoogleIdentity identity) {
        return userRepository.findWithPersonByEmail(identity.email()).map(user -> link(user, identity));
    }

    private UUID link(User user, GoogleIdentity identity) {
        userRepository.linkGoogle(user.getId(), identity.subject());
        return user.getUuid();
    }

    private UUID account(GoogleIdentity identity) {
        return linked(identity)
                .or(() -> bind(identity))
                .orElseThrow(() -> new CredentialException(CredentialProblem.GOOGLE_ACCOUNT_NOT_FOUND));
    }
}
