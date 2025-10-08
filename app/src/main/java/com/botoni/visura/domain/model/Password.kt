package com.botoni.visura.domain.model

import com.botoni.visura.domain.exceptions.AuthenticationException

class Password(val value: String) {
    init {
        if (value.isBlank()) {
            throw AuthenticationException("Senha é obrigatória")
        }
        if (value.length < 8) {
            throw AuthenticationException("Mínimo 8 caracteres")
        }
    }
    override fun toString(): String = value
}