package com.botoni.visura.domain.exceptions

enum class AuthError {
    VALIDATION,
    AUTHENTICATION,
    NETWORK,
    CANCELLED,
    INVALID_CREDENTIAL,
    NO_ACCOUNT_FOUND,
    EMAIL_ALREADY_IN_USE,
    TOO_MANY_REQUESTS,
    USER_DISABLED
}

sealed class AuthenticationException(
    val error: AuthError,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class ValidationError(message: String) :
        AuthenticationException(AuthError.VALIDATION, message)

    class NetworkError(
        message: String = "Erro de conexão. Verifique sua internet",
        cause: Throwable? = null
    ) :
        AuthenticationException(AuthError.NETWORK, message, cause)

    class UserCancelled(message: String = "Operação cancelada pelo usuário") :
        AuthenticationException(AuthError.CANCELLED, message)

    class GoogleSignInFailed(cause: Throwable) :
        AuthenticationException(AuthError.AUTHENTICATION, "Falha ao fazer login com Google", cause)

    class GoogleSignUpFailed(cause: Throwable) :
        AuthenticationException(AuthError.AUTHENTICATION, "Falha ao criar conta com Google", cause)

    class GoogleNoAccountFound :
        AuthenticationException(AuthError.NO_ACCOUNT_FOUND, "Nenhuma conta Google encontrada")

    class InvalidCredential :
        AuthenticationException(AuthError.INVALID_CREDENTIAL, "Email ou Senha inválida")

    class GoogleInvalidCredential :
        AuthenticationException(AuthError.INVALID_CREDENTIAL, "Credencial Google invalida")

    class EmailAlreadyInUse(message: String = "Este email já está em uso") :
        AuthenticationException(AuthError.EMAIL_ALREADY_IN_USE, message)

    class TooManyRequests(message: String = "Muitas tentativas. Tente novamente mais tarde") :
        AuthenticationException(AuthError.TOO_MANY_REQUESTS, message)

    class UserDisabled(message: String = "Esta conta foi desabilitada") :
        AuthenticationException(AuthError.USER_DISABLED, message)

}