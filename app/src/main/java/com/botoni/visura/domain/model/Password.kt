package com.botoni.visura.domain.model

import com.botoni.visura.domain.exceptions.AuthenticationException

class Password(val value: String) : Comparable<Password> {
    init {
        if (value.isBlank()) {
            throw AuthenticationException("Senha é obrigatória")
        }
        if (value.length < 8) {
            throw AuthenticationException("Mínimo 8 caracteres")
        }
    }

    override fun compareTo(other: Password): Int {
        return this.value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Password
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = value
}