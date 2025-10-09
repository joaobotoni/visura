package com.botoni.visura.domain.model

import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error

class Password(val value: String) : Comparable<Password> {
    init {
        if (value.isBlank()) {
            throw AuthenticationException("Senha é obrigatória", Error.VALIDATION)
        }
        if (value.length < 8) {
            throw AuthenticationException("Mínimo 8 caracteres", Error.VALIDATION)
        }
    }

    override fun compareTo(other: Password): Int {
        return this.value.compareTo(other.value)
    }
}