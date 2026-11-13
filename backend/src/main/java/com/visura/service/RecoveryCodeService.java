package com.visura.service;

import com.visura.database.entity.PasswordRecovery;
import com.visura.database.entity.User;
import com.visura.database.repository.PasswordRecoveryRepository;
import com.visura.exception.custom.CredentialException;
import com.visura.exception.enums.problem.CredentialProblem;
import com.visura.token.OpaqueToken;
import com.visura.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Códigos de recuperação de senha: 6 dígitos, válidos por 15 minutos, com no máximo 5 tentativas. Só o HMAC é gravado.
 */
@Service
@RequiredArgsConstructor
public class RecoveryCodeService {

    static final Duration VALIDITY = Duration.ofMinutes(15);
    static final int ATTEMPTS = 5;
    private static final int CODES = 1_000_000;
    private static final String FORMAT = "%06d";

    private final SecureRandom random = new SecureRandom();
    private final PasswordRecoveryRepository passwordRecoveryRepository;
    private final OpaqueToken opaqueToken;
    private final Clock clock;

    @Transactional
    public String issue(User user) {
        String code = generate();
        close(user);
        save(user, code);
        return code;
    }

    @Transactional(readOnly = true)
    public PasswordRecovery find(Email email) {
        return open(email);
    }

    @Transactional
    public void spend(PasswordRecovery recovery) {
        int spent = attempt(recovery);
        left(spent);
    }

    public void verify(PasswordRecovery recovery, String code) {
        matched(recovery, code);
    }

    @Transactional
    public void redeem(PasswordRecovery recovery) {
        int redeemed = finish(recovery);
        unused(redeemed);
    }

    private String generate() {
        return String.format(FORMAT, random.nextInt(CODES));
    }

    private void close(User user) {
        passwordRecoveryRepository.close(user.getId());
    }

    private void save(User user, String code) {
        passwordRecoveryRepository.save(create(user, code));
    }

    private PasswordRecovery create(User user, String code) {
        return PasswordRecovery.builder().user(user).codeHash(hash(code)).expiresAt(expiration()).build();
    }

    private byte[] hash(String code) {
        return opaqueToken.hash(code);
    }

    private Instant expiration() {
        return Instant.now(clock).plus(VALIDITY);
    }

    private int attempt(PasswordRecovery recovery) {
        return passwordRecoveryRepository.spend(recovery.getId(), ATTEMPTS);
    }

    private int finish(PasswordRecovery recovery) {
        return passwordRecoveryRepository.redeem(recovery.getId());
    }

    private PasswordRecovery open(Email email) {
        return passwordRecoveryRepository.findOpen(email, Instant.now(clock))
                .orElseThrow(() -> new CredentialException(CredentialProblem.INVALID_RECOVERY_CODE));
    }

    private static void left(int spent) {
        if (spent == 0) {
            throw new CredentialException(CredentialProblem.INVALID_RECOVERY_CODE);
        }
    }

    private void matched(PasswordRecovery recovery, String code) {
        if (!same(recovery, code)) {
            throw new CredentialException(CredentialProblem.INVALID_RECOVERY_CODE);
        }
    }

    private static void unused(int redeemed) {
        if (redeemed == 0) {
            throw new CredentialException(CredentialProblem.RECOVERY_USED);
        }
    }

    private boolean same(PasswordRecovery recovery, String code) {
        return MessageDigest.isEqual(recovery.getCodeHash(), hash(code));
    }
}
