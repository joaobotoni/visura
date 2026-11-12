package com.visura.support;

import com.visura.properties.RateLimitProperties;
import com.visura.ratelimit.RateLimitPolicy;
import com.visura.ratelimit.TokenBucket.Limit;

import java.time.Duration;

public final class Policies {

    private static final Limit ROOMY = new Limit(1_000, 1_000, Duration.ofSeconds(1));

    private Policies() {
    }

    public static RateLimitPolicy of(Limit account, Limit ceiling) {
        return RateLimitPolicy.from(new RateLimitProperties(ROOMY, ROOMY, ROOMY, ROOMY, account, ceiling, ROOMY, ROOMY, ROOMY));
    }

    public static RateLimitPolicy account(Limit account) {
        return of(account, ROOMY);
    }
}
