    package com.botoni.visura.data.repository

    import com.botoni.visura.data.datasource.FireBaseClientRemoteDataSource
    import javax.inject.Inject

    class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {
        suspend fun signUp(email: String, password: String) =
            fireBaseClientRemoteDataSource.signUp(email, password)

        suspend fun signIn(email: String, password: String) =
            fireBaseClientRemoteDataSource.signIn(email, password)

        fun signOut() =
            fireBaseClientRemoteDataSource.signOut()

    }