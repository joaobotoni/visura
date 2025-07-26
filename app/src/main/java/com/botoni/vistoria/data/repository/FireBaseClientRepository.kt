    package com.botoni.vistoria.data.repository

    import android.util.Log
    import com.botoni.vistoria.data.datasource.FireBaseClientRemoteDataSource
    import javax.inject.Inject

    class FireBaseClientRepository @Inject constructor(private val fireBaseClientRemoteDataSource: FireBaseClientRemoteDataSource) {
        suspend fun signUp(email: String, password: String) =
            fireBaseClientRemoteDataSource.signUp(email, password)

        suspend fun signIn(email: String, password: String) =
            fireBaseClientRemoteDataSource.signIn(email, password)

        fun signOut() =
            fireBaseClientRemoteDataSource.signOut()

    }