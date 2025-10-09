    package com.botoni.visura.domain.exceptions

    enum class Error {
        VALIDATION,
        AUTHENTICATION,
        NETWORK,
        CANCELLED,
        UNKNOWN
    }

    class AuthenticationException(message: String, error: Error) : Exception(message)