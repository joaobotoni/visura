package com.botoni.visura.domain.exceptions.location

sealed class LocationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class LocationNotFoundException(cause: Throwable? = null) :
        LocationException("Location not found", cause)

    class AddressNotFoundException(cause: Throwable? = null) :
        LocationException("Address not found", cause)
}