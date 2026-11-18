package com.visura.ui.navigation

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.visura.VisuraApplication
import com.visura.data.ApiErro
import com.visura.data.ContaGoogle
import com.visura.data.EnvioDeNarracoes
import com.visura.data.EnviosDeNarracao
import com.visura.data.Gravador
import com.visura.data.gerarLaudo
import com.visura.data.miniaturaDosBytes
import com.visura.data.prepararFoto
import com.visura.model.Ambiente
import com.visura.model.CategoriaImovel
import com.visura.model.EnderecoImovel
import com.visura.model.ItemAmbiente
import com.visura.model.PessoaVinculada
import com.visura.model.TipoDeAmbiente
import com.visura.model.TipoImovel
import com.visura.model.TipoVistoria
import com.visura.model.VistoriaCompleta
import com.visura.model.comItem
import com.visura.model.semItem
import com.visura.ui.screens.createaccount.DocumentosCadastro
import com.visura.ui.screens.createaccount.TipoPessoa
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Dados da primeira etapa do cadastro, guardados até a segunda etapa chamar a API
private data class ContaEmCadastro(val nome: String, val email: String, val senha: String)

// Narração em andamento: de qual ambiente, se está pausada e quanto já foi gravado. O cronômetro da tela soma
// "gravadoMs" ao tempo desde "retomadaEm" (relógio de SystemClock.elapsedRealtime) enquanto não está pausada.
data class Gravacao(
    val ambienteId: String,
    val ambienteNome: String,
    val pausada: Boolean = false,
    val gravadoMs: Long = 0,
    val retomadaEm: Long = SystemClock.elapsedRealtime()
)

// Algo que a tela precisa fazer uma única vez, em resposta a uma ação: mostrar um aviso, navegar
// ou abrir o compartilhamento. Fica num Channel até a tela estar visível para recebê-lo.
sealed interface Evento {
    data class Aviso(val mensagem: String) : Evento
    class Navegar(val comando: NavHostController.() -> Unit) : Evento
    class Compartilhar(val envio: Intent) : Evento
}

// Estado e ações do fluxo do app. Vive enquanto a Activity existir, inclusive quando o Android a recria
// (girar a tela, trocar o tema, abrir a câmera), então:
//  - uma ação da API iniciada continua até o fim, em vez de ser cancelada no meio;
//  - os dados compartilhados entre etapas (ambientes da vistoria, cadastro em duas etapas) não se perdem.
// Não guarda referência a Activity nem a NavController: o que depende da tela sai como Evento.
class VisuraViewModel(
    private val aplicacao: VisuraApplication,
    private val estadoSalvo: SavedStateHandle
) : ViewModel() {

    val repositorio = aplicacao.repositorio
    val buscaDeEndereco = aplicacao.buscaDeEndereco

    private val _ocupado = MutableStateFlow(false)

    // Enquanto uma ação roda, a tela fica bloqueada (evita enviar duas vezes)
    val ocupado: StateFlow<Boolean> = _ocupado.asStateFlow()

    private val _eventos = Channel<Evento>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    private val _ambientes = MutableStateFlow(emptyList<Ambiente>())

    // Ambientes da vistoria aberta: a lista, o detalhe do ambiente e o item leem e alteram os mesmos dados
    val ambientes: StateFlow<List<Ambiente>> = _ambientes.asStateFlow()

    // Miniaturas das evidências já enviadas, por id: a grade do item não baixa de novo a cada vez que abre.
    // O tamanho conta em KB; o teto é 1/8 da memória do app.
    private val miniaturas = object : LruCache<String, ImageBitmap>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()) {
        override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4 / 1024
    }

    private var contaEmCadastro: ContaEmCadastro? = null
    private var enderecoNovoImovel: EnderecoImovel? = null

    // Conta Google sem cadastro no Visura: nome e e-mail preenchem o cadastro
    var contaGoogle: ContaGoogle? = null
        private set

    val nomeEmCadastro: String get() = contaEmCadastro?.nome.orEmpty()

    // Recuperação de senha: e-mail e código conferido ficam no SavedStateHandle, que sobrevive
    // até ao Android encerrar o processo com o app em segundo plano.
    val emailRecuperacao: String get() = estadoSalvo[EMAIL_RECUPERACAO] ?: ""
    private val codigoRecuperacao: String get() = estadoSalvo[CODIGO_RECUPERACAO] ?: ""

    val logado: Boolean get() = repositorio.sessao.logado

    init {
        // Se o refresh token for recusado, a sessão acabou: volta para o login.
        viewModelScope.launch {
            repositorio.sessao.expirou.collect { expirou ->
                if (expirou) {
                    navegar { navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
                    avisar("Sua sessão expirou. Entre novamente.")
                }
            }
        }
    }

    fun avisar(mensagem: String) {
        _eventos.trySend(Evento.Aviso(mensagem))
    }

    private fun navegar(comando: NavHostController.() -> Unit) {
        _eventos.trySend(Evento.Navegar(comando))
    }

    // Roda uma ação da API (salvar, enviar, finalizar...) e transforma qualquer falha num aviso.
    private fun executar(acao: suspend () -> Unit) {
        if (_ocupado.value) return
        _ocupado.value = true
        viewModelScope.launch {
            try {
                acao()
            } catch (e: ApiErro) {
                avisar(e.message ?: "Algo deu errado.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Erro fora da API (arquivo, câmera, PDF...): avisa em vez de fechar o app
                avisar("Erro inesperado: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                _ocupado.value = false
            }
        }
    }

    // ---------------- Conta ----------------

    fun entrar(email: String, senha: String) = executar {
        repositorio.entrar(email, senha)
        navegar { navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
    }

    // A conta já foi escolhida no seletor do Google (que precisa da Activity e por isso roda na tela)
    fun entrarComGoogle(conta: ContaGoogle) = executar {
        try {
            repositorio.entrarComGoogle(conta.idToken)
            navegar { navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
        } catch (e: ApiErro) {
            if (e.status != 404) throw e
            // Sem conta no Visura: segue para o cadastro com nome e e-mail do Google
            contaGoogle = conta
            avisar(e.message ?: "Crie a sua conta para continuar.")
            navegar { navigate(Routes.CREATE_ACCOUNT_INFO) }
        }
    }

    fun novaConta() {
        contaGoogle = null
    }

    fun continuarCadastro(nome: String, email: String, senha: String) {
        contaEmCadastro = ContaEmCadastro(nome, email, senha)
        navegar { navigate(Routes.CREATE_ACCOUNT_DOCS) }
    }

    fun registrar(docs: DocumentosCadastro) {
        val conta = contaEmCadastro
        if (conta == null) {
            // O Android encerrou o app entre as etapas e a primeira se perdeu (a senha não é salva em disco)
            avisar("Os dados da primeira etapa se perderam. Preencha de novo, por favor.")
            navegar { popBackStack() }
            return
        }
        executar {
            repositorio.registrar(docs.nome, docs.cpf, conta.email, conta.senha)
            contaEmCadastro = null
            contaGoogle = null
            // A conta já existe; se a empresa falhar, segue para a Home e avisa.
            val papel = docs.papel
            if (docs.tipoPessoa == TipoPessoa.JURIDICA && papel != null) {
                try {
                    repositorio.cadastrarEmpresa(docs.razaoSocial, docs.nomeFantasia, docs.cnpj, papel.valorApi)
                } catch (e: ApiErro) {
                    avisar("Conta criada, mas a empresa não foi cadastrada: ${e.message}")
                }
            }
            navegar { navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } }
        }
    }

    fun sair() = executar {
        repositorio.sair()
        navegar { navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
    }

    // ---------------- Recuperação de senha ----------------

    fun pedirCodigo(email: String) = executar {
        repositorio.pedirCodigo(email)
        estadoSalvo[EMAIL_RECUPERACAO] = email
        navegar { navigate(Routes.FORGOT_PASSWORD_CODE) }
    }

    fun reenviarCodigo() = executar {
        repositorio.pedirCodigo(emailRecuperacao)
        avisar("Enviamos um código novo. O anterior deixou de valer.")
    }

    fun conferirCodigo(codigo: String) = executar {
        repositorio.conferirCodigo(emailRecuperacao, codigo)
        estadoSalvo[CODIGO_RECUPERACAO] = codigo
        navegar { navigate(Routes.FORGOT_PASSWORD_NEW) }
    }

    fun redefinirSenha(senha: String) = executar {
        val email = emailRecuperacao
        repositorio.redefinirSenha(email, codigoRecuperacao, senha)
        estadoSalvo[EMAIL_RECUPERACAO] = ""
        estadoSalvo[CODIGO_RECUPERACAO] = ""
        // A senha já mudou e o código não vale mais: se o login falhar, segue para a tela de login
        val destino = try {
            repositorio.entrar(email, senha)
            avisar("Senha alterada. Você já está conectado.")
            Routes.HOME
        } catch (_: ApiErro) {
            avisar("Senha alterada. Entre com a nova senha.")
            Routes.LOGIN
        }
        navegar { navigate(destino) { popUpTo(0) { inclusive = true } } }
    }

    // ---------------- Imóveis ----------------

    fun continuarImovel(endereco: EnderecoImovel) {
        enderecoNovoImovel = endereco
        navegar { navigate(Routes.NOVO_IMOVEL_TIPO) }
    }

    fun cadastrarImovel(tipo: TipoImovel, categoria: CategoriaImovel) {
        val endereco = enderecoNovoImovel
        if (endereco == null) {
            avisar("O endereço se perdeu. Preencha de novo, por favor.")
            navegar { popBackStack() }
            return
        }
        executar {
            val id = repositorio.cadastrarImovel(endereco, tipo, categoria)
            enderecoNovoImovel = null
            // O popUpTo remove as telas do cadastro, então a seta de voltar leva para a Home.
            navegar { navigate(Routes.imovelDetalhe(id)) { popUpTo(Routes.HOME) } }
        }
    }

    // ---------------- Vistoria ----------------

    fun iniciarVistoria(imovelId: String, tipo: TipoVistoria, observacoes: String) = executar {
        val vistoriaId = repositorio.iniciarVistoria(imovelId, tipo, observacoes)
        // Tira esta etapa da pilha para o "voltar" não criar outra vistoria
        navegar {
            navigate(Routes.pessoasVinculadas(imovelId, vistoriaId)) {
                popUpTo(Routes.NOVA_VISTORIA) { inclusive = true }
            }
        }
    }

    fun cadastrarPessoa(pessoa: PessoaVinculada, cpf: String, cnpj: String, onCriada: (PessoaVinculada) -> Unit) =
        executar { onCriada(repositorio.cadastrarPessoa(pessoa, cpf, cnpj)) }

    fun vincularPessoas(vistoriaId: String, imovelId: String, pessoas: List<PessoaVinculada>) = executar {
        repositorio.vincularPessoas(vistoriaId, imovelId, pessoas)
        navegar { navigate(Routes.ambientes(vistoriaId)) }
    }

    // A lista de ambientes é sempre recarregada da API ao abrir a etapa
    suspend fun carregarAmbientes(vistoriaId: String): List<Ambiente> =
        repositorio.ambientes(vistoriaId).also { _ambientes.value = it }

    fun ambiente(id: String?): Ambiente? = _ambientes.value.find { it.id == id }

    fun removerAmbiente(ambienteId: String) = executar {
        repositorio.removerAmbiente(ambienteId)
        _ambientes.update { lista -> lista.filterNot { it.id == ambienteId } }
    }

    fun adicionarAmbiente(vistoriaId: String, nome: String, tipo: TipoDeAmbiente?, observacoes: String, duplicarDe: Ambiente?) =
        executar {
            val novo = repositorio.adicionarAmbiente(vistoriaId, nome, tipo?.id, observacoes, duplicarDe)
            _ambientes.update { it + novo }
            navegar { popBackStack() }
        }

    fun removerItem(ambienteId: String, itemId: String) = executar {
        repositorio.removerItem(itemId)
        _ambientes.update { it.semItem(ambienteId, itemId) }
    }

    // Cria o item e envia as fotos escolhidas antes de salvar
    fun criarItem(ambienteId: String, item: ItemAmbiente, fotos: List<Uri>) = executar {
        val criado = repositorio.salvarItem(ambienteId, item.copy(fotos = 0), novo = true)
        _ambientes.update { it.comItem(ambienteId, criado) }
        // O item já existe; uma foto que falhar não desfaz o item, só avisa
        val enviadas = mutableListOf<String>()
        try {
            fotos.forEach { uri ->
                enviadas += enviarEGuardarMiniatura(ambienteId, criado.id, uri)
            }
        } catch (e: ApiErro) {
            avisar("Item salvo, mas ${fotos.size - enviadas.size} foto(s) não foram enviadas: ${e.message}")
        }
        _ambientes.update { it.comItem(ambienteId, criado.copy(fotos = enviadas.size, fotosIds = enviadas)) }
        navegar { popBackStack() }
    }

    fun salvarItem(ambienteId: String, item: ItemAmbiente) = executar {
        val salvo = repositorio.salvarItem(ambienteId, item, novo = false)
        _ambientes.update { it.comItem(ambienteId, salvo) }
        navegar { popBackStack() }
    }

    // Foto de um item que já existe: vai direto para a API
    fun enviarFoto(ambienteId: String, itemId: String, uri: Uri, onEnviada: () -> Unit) = executar {
        val fotoId = enviarEGuardarMiniatura(ambienteId, itemId, uri)
        onEnviada()
        // Atualiza a contagem também na lista, caso o usuário volte sem salvar
        ambiente(ambienteId)?.itens?.find { it.id == itemId }?.let { atual ->
            _ambientes.update {
                it.comItem(ambienteId, atual.copy(fotos = atual.fotos + 1, fotosIds = atual.fotosIds + fotoId))
            }
        }
        avisar("Foto enviada.")
    }

    // Envia a foto e já guarda a miniatura do JPEG enviado, para a grade não precisar baixá-lo
    private suspend fun enviarEGuardarMiniatura(ambienteId: String, itemId: String, uri: Uri): String {
        val jpeg = prepararFoto(aplicacao, uri)
        val fotoId = repositorio.enviarFoto(ambienteId, itemId, jpeg)
        miniaturaDosBytes(jpeg)?.let { miniaturas.put(fotoId, it) }
        return fotoId
    }

    // Miniatura de uma evidência já enviada: do cache ou baixada da API. null se não der para baixar.
    suspend fun miniatura(evidenciaId: String): ImageBitmap? {
        miniaturas.get(evidenciaId)?.let { return it }
        val baixada = try {
            miniaturaDosBytes(repositorio.baixarFoto(evidenciaId))
        } catch (_: ApiErro) {
            null
        }
        return baixada?.also { miniaturas.put(evidenciaId, it) }
    }

    // ---------------- Narração ----------------

    private val gravador = Gravador(aplicacao)
    private val _gravacao = MutableStateFlow<Gravacao?>(null)

    // A gravação vive no ViewModel: continua ao girar a tela e enquanto o vistoriador abre os itens do ambiente
    val gravacao: StateFlow<Gravacao?> = _gravacao.asStateFlow()

    fun gravarNarracao(ambiente: Ambiente) {
        if (_gravacao.value != null) return
        try {
            gravador.iniciar()
            _gravacao.value = Gravacao(ambiente.id, ambiente.nome)
        } catch (e: ApiErro) {
            avisar(e.message ?: "Não foi possível gravar.")
        }
    }

    fun pausarNarracao() {
        val atual = _gravacao.value ?: return
        if (atual.pausada) return
        gravador.pausar()
        _gravacao.value = atual.copy(pausada = true, gravadoMs = atual.gravadoMs + SystemClock.elapsedRealtime() - atual.retomadaEm)
    }

    fun continuarNarracao() {
        val atual = _gravacao.value ?: return
        if (!atual.pausada) return
        gravador.continuar()
        _gravacao.value = atual.copy(pausada = false, retomadaEm = SystemClock.elapsedRealtime())
    }

    // Encerra a gravação e põe o arquivo na fila de envio, que segue em segundo plano
    fun concluirNarracao() {
        val atual = _gravacao.value ?: return
        _gravacao.value = null
        val arquivo = gravador.concluir()
        if (arquivo == null) {
            avisar("A narração ficou vazia e foi descartada.")
            return
        }
        EnvioDeNarracoes.agendar(aplicacao, atual.ambienteId, arquivo)
        avisar("Narração salva. O envio continua em segundo plano, mesmo sem internet agora.")
    }

    fun descartarNarracao() {
        _gravacao.value = null
        gravador.descartar()
    }

    fun enviosDeNarracao(ambienteId: String): Flow<EnviosDeNarracao> = EnvioDeNarracoes.acompanhar(aplicacao, ambienteId)

    // O ViewModel só acaba quando a Activity sai de vez: a narração em andamento é salva, não perdida
    override fun onCleared() {
        concluirNarracao()
    }

    fun finalizarVistoria(vistoriaId: String) = executar {
        repositorio.finalizarVistoria(vistoriaId)
        // Tira as etapas da pilha: depois da conclusão, voltar leva para a Home
        navegar { navigate(Routes.conclusao(vistoriaId)) { popUpTo(Routes.HOME) } }
    }

    // Laudo de uma vistoria já concluída, aberto pelo histórico do imóvel
    fun compartilharLaudo(vistoriaId: String) = executar {
        compartilhar(repositorio.vistoriaCompleta(vistoriaId))
    }

    // Laudo da vistoria que acabou de ser concluída; depois do compartilhamento, volta para a Home
    fun compartilharLaudoEVoltar(vistoria: VistoriaCompleta) = executar {
        compartilhar(vistoria)
        navegar { popBackStack(Routes.HOME, inclusive = false) }
    }

    // Gera o laudo em PDF no aparelho, com as fotos baixadas da API, e pede à tela que abra o compartilhamento
    private suspend fun compartilhar(vistoria: VistoriaCompleta) {
        val pdf = gerarLaudo(aplicacao, vistoria) { repositorio.baixarFoto(it) }
        val uri = FileProvider.getUriForFile(aplicacao, "${aplicacao.packageName}.arquivos", pdf)
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laudo de vistoria — ${vistoria.imovel?.nome.orEmpty()}")
            clipData = ClipData.newRawUri("laudo", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        _eventos.send(Evento.Compartilhar(envio))
    }

    companion object {
        private const val EMAIL_RECUPERACAO = "email_recuperacao"
        private const val CODIGO_RECUPERACAO = "codigo_recuperacao"

        // Sem injeção de dependência: o repositório vem da Application, criado uma vez só
        val Fabrica = viewModelFactory {
            initializer {
                VisuraViewModel(this[APPLICATION_KEY] as VisuraApplication, createSavedStateHandle())
            }
        }
    }
}
