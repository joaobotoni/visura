package com.visura.service;

import com.visura.database.entity.LocalCredential;
import com.visura.exception.custom.RateLimitException;
import com.visura.exception.enums.problem.RateLimitProblem;
import com.visura.ratelimit.RateLimitPolicy;
import com.visura.ratelimit.TokenBucket;
import com.visura.ratelimit.TokenBucket.Allowance;
import com.visura.vo.Password;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordAttemptService {

    private static final String KEY = "password:%s";

    private final RateLimitPolicy rateLimitPolicy;
    private final LocalCredentialService localCredentialService;

    public LocalCredential verify(UUID user, Password password) {
        Allowance allowance = allow(user);
        within(allowance);
        return credential(user, password);
    }

    private Allowance allow(UUID user) {
        return rateLimitPolicy.account().allow(key(user));
    }

    private static String key(UUID user) {
        return String.format(KEY, user);
    }

    private LocalCredential credential(UUID user, Password password) {
        return localCredentialService.verify(user, password);
    }

    private static void within(Allowance allowance) {
        if (!allowance.allowed()) {
            throw new RateLimitException(RateLimitProblem.EXCEEDED, TokenBucket.retry(allowance));
        }
    }
}
