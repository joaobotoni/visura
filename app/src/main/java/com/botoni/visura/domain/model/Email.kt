package com.botoni.visura.domain.model

import android.util.Patterns
import com.botoni.visura.domain.exceptions.AuthenticationException


@JvmInline
value class Email(val value: String) : Comparable<Email> {
    companion object {
        fun create(value: String): Result<Email> {
            if (value.isBlank()) {
                return Result.failure(
                    AuthenticationException.ValidationError("E-mail não informado")
                )
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                return Result.failure(
                    AuthenticationException.ValidationError("E-mail inválido")
                )
            }
            return Result.success(Email(value))
        }

        fun access(value: String): Result<Email> {
            if (value.isBlank()) {
                return Result.failure(
                    AuthenticationException.ValidationError("E-mail não informado")
                )
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                return Result.failure(
                    AuthenticationException.ValidationError("E-mail é inválido")
                )
            }
            return Result.success(Email(value))
        }
    }

    override fun compareTo(other: Email): Int = value.compareTo(other.value)
}