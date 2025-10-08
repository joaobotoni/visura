package com.botoni.visura.domain.model

import android.util.Patterns
import com.botoni.visura.domain.exceptions.AuthenticationException

class Email(val value: String) {
    init {
        if (value.isBlank()) {
            throw AuthenticationException("E-mail é obrigatório")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            throw AuthenticationException("Formato de e-mail inválido")
        }
    }
    override fun toString(): String = value
}