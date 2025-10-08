    package com.botoni.visura.data.repository

    import com.botoni.visura.data.datasource.FireBaseClientRemoteDataSource
    import com.botoni.visura.domain.model.Email
    import com.botoni.visura.domain.model.Password
    import javax.inject.Inject

    class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {
        suspend fun signUp(email: Email, password: Password) = fireBaseClientRemoteDataSource.signUp(email, password)
        suspend fun signIn(email: Email, password: Password) = fireBaseClientRemoteDataSource.signIn(email, password)
        fun signOut() = fireBaseClientRemoteDataSource.signOut()
    }