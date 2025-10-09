package com.botoni.visura.domain.model

import android.util.Patterns
import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error


@JvmInline
value class Email(private val value: String) : Comparable<Email> {

    init {
        require(value.isNotBlank()) {
            throw AuthenticationException(
                Error.VALIDATION,
                "E-mail é obrigatório"
            )
        }
        require(Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            throw AuthenticationException(
                Error.VALIDATION,
                "E-mail é inválido"
            )
        }
    }

    override fun compareTo(other: Email): Int = value.compareTo(other.value)
}
