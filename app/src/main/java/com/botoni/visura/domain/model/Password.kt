package com.botoni.visura.domain.model

import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error

@JvmInline
value class Password(private val value: String) : Comparable<Password> {

    init {
        require(value.isNotBlank()) {
            throw AuthenticationException(Error.VALIDATION, "Senha é obrigatória")
        }
        require(value.length >= MIN_LENGTH) {
            throw AuthenticationException(Error.VALIDATION, "Mínimo $MIN_LENGTH caracteres")
        }
    }

    override fun compareTo(other: Password): Int = value.compareTo(other.value)

    companion object {
        private const val MIN_LENGTH = 8
    }
}
