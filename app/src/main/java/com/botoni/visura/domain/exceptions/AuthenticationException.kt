    package com.botoni.visura.domain.exceptions

    enum class Error{
        VALIDATION, AUTHENTICATION, NETWORK, CANCELLED, UNKNOWN
    }
    class AuthenticationException(error: Error, message: String) : Exception(message)