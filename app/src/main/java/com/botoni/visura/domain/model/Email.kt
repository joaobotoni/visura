package com.botoni.visura.domain.model

import android.util.Patterns
import com.botoni.visura.domain.exceptions.AuthenticationException
import com.botoni.visura.domain.exceptions.Error

class Email(val value: String) : Comparable<Email> {
    init {
        if (value.isBlank()) {
            throw AuthenticationException("E-mail é obrigatório", Error.VALIDATION)
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            throw AuthenticationException("Formato de e-mail inválido", Error.VALIDATION)
        }
    }

    override fun compareTo(other: Email): Int {
        return this.value.compareTo(other.value)
    }
}