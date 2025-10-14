package com.botoni.visura.data.datasource.authentication

import com.botoni.visura.domain.exceptions.authentication.AuthenticationException
import com.botoni.visura.domain.model.authentication.Email
import com.botoni.visura.domain.model.authentication.Password
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireBaseClientRemoteDataSource @Inject constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    suspend fun signUp(email: Email, password: Password): AuthResult {
        return try {
            auth.createUserWithEmailAndPassword(email.value, password.value).await()
        } catch (e: FirebaseAuthException) {
            throw mapFirebaseException(e)
        } catch (e: FirebaseNetworkException) {
            throw AuthenticationException.NetworkError(cause = e)
        }
    }

    suspend fun signIn(email: Email, password: Password): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email.value, password.value).await()
        } catch (e: FirebaseAuthException) {
            throw mapFirebaseException(e)
        } catch (e: FirebaseNetworkException) {
            throw AuthenticationException.NetworkError(cause = e)
        }
    }

    fun signOut() = auth.signOut()

    private fun mapFirebaseException(exception: FirebaseAuthException): AuthenticationException =
        errorMap[exception.errorCode] ?: throw exception

    private val errorMap = mapOf(
        "ERROR_EMAIL_ALREADY_IN_USE" to AuthenticationException.EmailAlreadyInUse(),
        "ERROR_TOO_MANY_REQUESTS" to AuthenticationException.TooManyRequests(),
        "ERROR_USER_DISABLED" to AuthenticationException.UserDisabled(),
        "ERROR_INVALID_CREDENTIAL" to AuthenticationException.InvalidCredential()
    )
}