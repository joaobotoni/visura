package com.botoni.visura.domain.model.authentication

import androidx.compose.ui.graphics.RectangleShape
import com.botoni.visura.domain.exceptions.authentication.AuthenticationException

@JvmInline
value class Password(val value: String) : Comparable<Password> {
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

        fun confirm(value: String, other: String): Result<Password> {
            if (value.isBlank()) {
                return Result.failure(
                    AuthenticationException.ValidationError("Confirmação de senha não informada")
                )
            }
            if (value != other) {
                return Result.failure(
                    AuthenticationException.ValidationError("As senhas não coincidem")
                )
            }
            return Result.success(Password(value))
        }
    }

    override fun compareTo(other: Password): Int = value.compareTo(other.value)
}
