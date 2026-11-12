package com.visura

import android.app.Application
import com.visura.data.Sessao
import com.visura.data.VisuraRepositorio
import com.visura.data.remote.ApiClient

// Cria uma única vez a sessão, o cliente da API e o repositório que as telas usam.
class VisuraApplication : Application() {

    lateinit var repositorio: VisuraRepositorio
        private set

    override fun onCreate() {
        super.onCreate()
        val sessao = Sessao(this)
        repositorio = VisuraRepositorio(ApiClient.criar(sessao), sessao)
    }
}
