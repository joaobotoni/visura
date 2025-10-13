package com.botoni.visura.domain.model

import com.botoni.visura.domain.exceptions.AuthenticationException

@JvmInline
value class Password(val value: String) : Comparable<Password> {
    fun matches(confirmation: Password): Result<Unit> {
        return if (this.value == confirmation.value) {
            Result.success(Unit)
        } else {
            Result.failure(AuthenticationException.ValidationError("As senhas não coincidem"))
        }
    }

    companion object {
        fun create(value: String): Result<Password> {

            if (value.isBlank()) {
                return Result.failure(
                    AuthenticationException.ValidationError("Senha não informada")
                )
            }

            if (value.length < 8) {
                return Result.failure(
                    AuthenticationException.ValidationError("Senha deve ter pelo menos 8 caracteres")
                )
            }

            if (!value.any { it.isDigit() }) {
                return Result.failure(
                    AuthenticationException.ValidationError("Senha deve conter pelo menos um número")
                )
            }

            return Result.success(Password(value))
        }

        fun access(value: String): Result<Password> {
            if (value.isBlank()) {
                return Result.failure(
                    AuthenticationException.ValidationError("Senha não informada")
                )
            }
            return Result.success(Password(value))
        }
    }

    override fun compareTo(other: Password): Int = value.compareTo(other.value)
}
