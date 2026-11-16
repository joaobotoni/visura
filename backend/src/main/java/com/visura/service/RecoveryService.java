package com.visura.service;

import com.visura.database.entity.PasswordRecovery;
import com.visura.database.entity.User;
import com.visura.database.repository.UserRepository;
import com.visura.exception.custom.RateLimitException;
import com.visura.exception.custom.SenderException;
import com.visura.exception.enums.problem.RateLimitProblem;
import com.visura.ratelimit.RateLimitPolicy;
import com.visura.ratelimit.TokenBucket;
import com.visura.ratelimit.TokenBucket.Allowance;
import com.visura.vo.Email;
import com.visura.vo.Mail;
import com.visura.vo.NewPassword;
import com.visura.vo.PasswordHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Recuperação de senha sem estar logado: o código vai para o e-mail da conta e, conferido, permite gravar uma senha
 * nova. E-mail sem conta responde igual a e-mail com conta (o pedido nunca revela se a conta existe), e todo pedido,
 * certo ou errado, gasta o limite por conta ({@code RATE_LIMIT_ACCOUNT_*}, chave {@code recovery:<e-mail>}). Por isso a
 * falha no envio do e-mail também não muda a resposta: fica só no log, e o usuário pode pedir outro código.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryService {

    private static final String KEY = "recovery:%s";
    private static final String SUBJECT = "Código para redefinir sua senha do Visura";
    private static final String BODY = """
            Olá, %s.

            Use o código %s no app Visura para criar uma nova senha. Ele vale por %d minutos.

            Se você não pediu para trocar a senha, ignore este e-mail: a sua senha continua a mesma.
            """;

    private final RateLimitPolicy rateLimitPolicy;
    private final UserRepository userRepository;
    private final RecoveryCodeService recoveryCodeService;
    private final LocalCredentialService localCredentialService;
    private final PasswordService passwordService;
    private final MailService mailService;

    public void request(Email email) {
        Allowance allowance = allow(email);
        within(allowance);
        recover(email);
    }

    public void confirm(Email email, String code) {
        checked(email, code);
    }

    public void reset(Email email, String code, NewPassword password) {
        PasswordRecovery recovery = checked(email, code);
        PasswordHash hash = hash(password);
        restore(recovery, hash);
    }

    private Allowance allow(Email email) {
        return rateLimitPolicy.account().allow(String.format(KEY, email.value()));
    }

    private void recover(Email email) {
        userRepository.findWithPersonByEmail(email).ifPresent(this::deliver);
    }

    private void deliver(User user) {
        String code = issue(user);
        send(mail(user, code));
    }

    private String issue(User user) {
        return recoveryCodeService.issue(user);
    }

    private static Mail mail(User user, String code) {
        String body = BODY.formatted(user.getPerson().getName(), code, RecoveryCodeService.VALIDITY.toMinutes());
        return new Mail(user.getEmail(), SUBJECT, body);
    }

    private PasswordRecovery checked(Email email, String code) {
        within(allow(email));
        PasswordRecovery recovery = find(email);
        spend(recovery);
        verify(recovery, code);
        return recovery;
    }

    private PasswordRecovery find(Email email) {
        return recoveryCodeService.find(email);
    }

    private void spend(PasswordRecovery recovery) {
        recoveryCodeService.spend(recovery);
    }

    private void verify(PasswordRecovery recovery, String code) {
        recoveryCodeService.verify(recovery, code);
    }

    private PasswordHash hash(NewPassword password) {
        return localCredentialService.hash(password.password());
    }

    private void restore(PasswordRecovery recovery, PasswordHash hash) {
        passwordService.restore(recovery, hash);
    }

    private static void within(Allowance allowance) {
        if (!allowance.allowed()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, TokenBucket.retry(allowance));
        }
    }

    private void send(Mail mail) {
        try {
            mailService.send(mail);
        } catch (SenderException exception) {
            log.error("Falha ao enviar o código de recuperação de senha", exception);
        }
    }
}
