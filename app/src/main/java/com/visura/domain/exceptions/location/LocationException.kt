package com.visura.domain.exceptions.location

// LocationException.kt
sealed class LocationException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    class LocationNotFoundException(cause: Throwable? = null) :
        LocationException("Localização não disponível", cause)

    class AddressNotFoundException(cause: Throwable? = null) :
        LocationException("Endereço não encontrado", cause)
}