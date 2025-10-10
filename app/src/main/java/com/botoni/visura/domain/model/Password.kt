package com.botoni.visura.domain.model

import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error

@JvmInline
value class Password(val value: String) : Comparable<Password> {
    init {
        require(value.isBlank()) {
            throw AuthenticationException(
                Error.VALIDATION,
                "Senha não informada"
            )
        }
        require(value.length >= 8) {
            throw AuthenticationException(
                Error.VALIDATION,
                "Senha deve ter pelo menos 8 caracteres"
            )
        }
        require(value.any { it.isDigit() }) {
            throw AuthenticationException(
                Error.VALIDATION,
                "Senha deve conter pelo menos um número"
            )
        }
    }

    override fun compareTo(other: Password): Int = value.compareTo(other.value)
}
