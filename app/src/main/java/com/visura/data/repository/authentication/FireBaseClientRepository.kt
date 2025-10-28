package com.visura.data.repository.authentication

import com.visura.data.datasource.authentication.FireBaseClientRemoteDataSource
import com.visura.domain.model.authentication.Email
import com.visura.domain.model.authentication.Password
import javax.inject.Inject

class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {
    suspend fun signUp(email: Email, password: Password) =
        fireBaseClientRemoteDataSource.signUp(email, password)

    suspend fun signIn(email: Email, password: Password) =
        fireBaseClientRemoteDataSource.signIn(email, password)

    fun signOut() = fireBaseClientRemoteDataSource.signOut()
}