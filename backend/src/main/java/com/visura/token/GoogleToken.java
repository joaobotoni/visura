package com.visura.token;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.visura.exception.custom.CredentialException;
import com.visura.exception.enums.problem.CredentialProblem;
import com.visura.vo.Email;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Confere o ID token que o app recebe do Google (assinatura, emissor, validade e o client ID do Visura como audiência)
 * e devolve a identidade com o e-mail já confirmado pelo Google. Sem client ID configurado, o login com Google fica
 * desligado ({@code 503}).
 */
public final class GoogleToken {

    private final GoogleIdTokenVerifier verifier;

    public GoogleToken(String clientId) {
        this.verifier = verifier(clientId);
    }

    public GoogleIdentity verify(String idToken) {
        GoogleIdToken.Payload payload = payload(available(), idToken);
        confirmed(payload);
        return new GoogleIdentity(payload.getSubject(), Email.of(payload.getEmail()));
    }

    private static GoogleIdTokenVerifier verifier(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    private GoogleIdTokenVerifier available() {
        if (verifier == null) {
            throw new CredentialException(CredentialProblem.GOOGLE_UNAVAILABLE);
        }
        return verifier;
    }

    private static GoogleIdToken.Payload payload(GoogleIdTokenVerifier verifier, String idToken) {
        try {
            return valid(verifier.verify(idToken)).getPayload();
        } catch (GeneralSecurityException | IOException | IllegalArgumentException exception) {
            throw new CredentialException(CredentialProblem.GOOGLE_INVALID);
        }
    }

    private static GoogleIdToken valid(GoogleIdToken token) {
        if (token == null) {
            throw new CredentialException(CredentialProblem.GOOGLE_INVALID);
        }
        return token;
    }

    private static void confirmed(GoogleIdToken.Payload payload) {
        if (!Boolean.TRUE.equals(payload.getEmailVerified()) || payload.getEmail() == null) {
            throw new CredentialException(CredentialProblem.GOOGLE_INVALID);
        }
    }

    /** A conta do Google: o identificador estável ({@code sub}) e o e-mail confirmado. */
    public record GoogleIdentity(String subject, Email email) {
    }
}
