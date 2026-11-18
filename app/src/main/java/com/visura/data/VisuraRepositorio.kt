package com.visura.data

import android.os.Build
import com.visura.data.remote.AddressRequest
import com.visura.data.remote.AddressResponse
import com.visura.data.remote.AuthenticationResponse
import com.visura.data.remote.CompanyAccountRequest
import com.visura.data.remote.DeviceRequest
import com.visura.data.remote.GoogleLoginRequest
import com.visura.data.remote.RecoveryCodeRequest
import com.visura.data.remote.RecoveryRequest
import com.visura.data.remote.ResetPasswordRequest
import com.visura.data.remote.IndividualRequest
import com.visura.data.remote.InspectionDetailResponse
import com.visura.data.remote.InspectionRequest
import com.visura.data.remote.InspectionResponse
import com.visura.data.remote.ItemRequest
import com.visura.data.remote.ItemResponse
import com.visura.data.remote.LoginRequest
import com.visura.data.remote.OwnerRequest
import com.visura.data.remote.PageResponse
import com.visura.data.remote.ParticipantRequest
import com.visura.data.remote.PersonCompanyRequest
import com.visura.data.remote.PersonResponse
import com.visura.data.remote.PersonSummaryResponse
import com.visura.data.remote.PropertyRequest
import com.visura.data.remote.PropertyResponse
import com.visura.data.remote.RegisterRequest
import com.visura.data.remote.RoomRequest
import com.visura.data.remote.RoomResponse
import com.visura.data.remote.VisuraApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.visura.model.Ambiente
import com.visura.model.CategoriaImovel
import com.visura.model.EnderecoImovel
import com.visura.model.EstadoItem
import com.visura.model.Imovel
import com.visura.model.ItemAmbiente
import com.visura.model.Narracao
import com.visura.model.PapelPessoa
import com.visura.model.Perfil
import com.visura.model.PessoaVinculada
import com.visura.model.StatusVistoria
import com.visura.model.TipoDeAmbiente
import com.visura.model.TipoImovel
import com.visura.model.TipoVistoria
import com.visura.model.Vistoria
import com.visura.model.VistoriaCompleta
import com.visura.model.VistoriaHistorico
import com.visura.model.itensPadrao

// Ponte entre as telas e a API: chama os endpoints e converte as respostas nos modelos que as telas já usam.
// Toda função lança ApiErro com uma mensagem pronta para o usuário.
class VisuraRepositorio(private val api: VisuraApi, val sessao: Sessao) {

    // ---------------- Autenticação ----------------

    suspend fun entrar(email: String, senha: String) = chamar {
        abrirSessao(api.entrar(LoginRequest(email.trim(), senha, dispositivo())))
    }

    // Login com Google: a API confere o ID token e entra na conta que usa o mesmo e-mail.
    suspend fun entrarComGoogle(idToken: String) = chamar {
        abrirSessao(api.entrarComGoogle(GoogleLoginRequest(idToken, dispositivo())))
    }

    // Recuperação de senha: a API responde igual exista a conta ou não.
    suspend fun pedirCodigo(email: String) = chamar {
        api.pedirCodigo(RecoveryRequest(email.trim())).exigirSucesso()
    }

    suspend fun conferirCodigo(email: String, codigo: String) = chamar {
        api.conferirCodigo(RecoveryCodeRequest(email.trim(), codigo)).exigirSucesso()
    }

    // Grava a nova senha. A API encerra todas as sessões; quem chama entra em seguida com a senha nova.
    suspend fun redefinirSenha(email: String, codigo: String, senha: String) = chamar {
        api.redefinirSenha(ResetPasswordRequest(email.trim(), codigo, senha)).exigirSucesso()
    }

    suspend fun registrar(nome: String, cpf: String, email: String, senha: String) = chamar {
        abrirSessao(api.registrar(RegisterRequest(nome.trim(), cpf, email.trim(), senha, dispositivo())))
        sessao.cpf = cpf
    }

    // Cadastro como pessoa jurídica: depois de criar o usuário, cria a empresa com o papel escolhido
    // (sócio, representante ou MEI, os papéis de administrador).
    suspend fun cadastrarEmpresa(razaoSocial: String, nomeFantasia: String, cnpj: String, papel: String) = chamar {
        api.cadastrarEmpresa(CompanyAccountRequest(razaoSocial.trim(), nomeFantasia.trim(), cnpj, papel))
        Unit
    }

    suspend fun sair() {
        // Mesmo se a API não responder, a sessão local é encerrada.
        try {
            chamar { api.sair() }
        } catch (_: ApiErro) {
        }
        sessao.limpar()
    }

    private fun abrirSessao(resposta: AuthenticationResponse) {
        val token = resposta.token
        val access = token?.accessToken
        val refresh = token?.refreshToken
        if (access == null || refresh == null) throw ApiErro("Resposta de login inválida do servidor.")
        sessao.salvarTokens(access, refresh)
        sessao.salvarUsuario(resposta.user?.name.orEmpty(), resposta.user?.email.orEmpty())
    }

    private fun dispositivo() = DeviceRequest(
        identifier = sessao.dispositivo,
        platform = "android",
        manufacturer = Build.MANUFACTURER.ifBlank { "desconhecido" }.take(64),
        model = Build.MODEL.ifBlank { "desconhecido" }.take(64),
        osVersion = Build.VERSION.RELEASE.ifBlank { Build.VERSION.SDK_INT.toString() }.take(16)
    )

    // ---------------- Perfil e Home ----------------

    suspend fun perfil(): Perfil = chamar {
        coroutineScope {
            val usuario = async { api.usuarioAtual() }
            val empresas = async { api.empresas() }
            val u = usuario.await()
            sessao.salvarUsuario(u.name.orEmpty(), u.email.orEmpty())
            Perfil(
                nome = u.name.orEmpty(),
                email = u.email.orEmpty(),
                cpf = (u.cpf ?: sessao.cpf)?.let(::formatarCpf).orEmpty(),
                cnpj = empresas.await().firstOrNull()?.cnpj?.let(::formatarCnpj).orEmpty()
            )
        }
    }

    // Vistorias do usuário para a Home. A API já devolve o andamento e o responsável de cada uma
    // (numa empresa, pode ser outro membro), então bastam duas chamadas.
    suspend fun vistoriasDaHome(): List<Vistoria> = chamar {
        coroutineScope {
            val vistorias = async { todasAsPaginas { api.vistorias(page = it) } }
            val imoveis = async { todasAsPaginas { api.imoveis(page = it) }.associateBy { it.id } }
            val lista = vistorias.await()
            val porId = imoveis.await()

            lista.map { v ->
                val imovel = porId[v.property]
                Vistoria(
                    id = v.id.orEmpty(),
                    imovelId = v.property.orEmpty(),
                    titulo = imovel?.let(::nomeDoImovel) ?: "Imóvel",
                    endereco = imovel?.address?.let(::enderecoCurto).orEmpty(),
                    status = statusDe(v.status),
                    data = formatarData(v.startedAt),
                    ambientesFeitos = v.progress?.completed ?: 0,
                    ambientesTotais = v.progress?.rooms ?: 0,
                    responsavel = v.responsible?.name ?: sessao.usuario?.nome.orEmpty()
                )
            }
        }
    }

    // ---------------- Imóveis ----------------

    suspend fun imovel(id: String): Imovel = chamar {
        coroutineScope {
            val imovel = async { api.imovel(id) }
            val donos = async { api.proprietarios(id) }
            val vistorias = async { todasAsPaginas { api.vistorias(imovel = id, page = it) } }
            montarImovel(imovel.await(), donos.await(), vistorias.await())
        }
    }

    suspend fun cadastrarImovel(endereco: EnderecoImovel, tipo: TipoImovel, categoria: CategoriaImovel): String = chamar {
        val criado = api.cadastrarImovel(
            PropertyRequest(
                description = null,
                type = tipo.valorApi,
                category = categoria.valorApi,
                address = AddressRequest(
                    cep = endereco.cep,
                    state = endereco.uf,
                    city = endereco.cidade,
                    district = endereco.bairro,
                    street = endereco.rua,
                    number = endereco.numero,
                    complement = endereco.complemento.ifBlank { null }
                )
            )
        )
        criado.id ?: throw ApiErro("O servidor não devolveu o imóvel criado.")
    }

    private fun montarImovel(
        imovel: PropertyResponse,
        donos: List<PersonSummaryResponse>,
        vistorias: List<InspectionResponse>
    ) = Imovel(
        id = imovel.id.orEmpty(),
        nome = nomeDoImovel(imovel),
        endereco = imovel.address?.let(::enderecoCompleto).orEmpty(),
        tipo = TipoImovel.entries.firstOrNull { it.valorApi == imovel.type }?.rotulo ?: imovel.type.orEmpty(),
        finalidade = CategoriaImovel.entries.firstOrNull { it.valorApi == imovel.category }?.rotulo ?: imovel.category.orEmpty(),
        pessoas = donos.map { it.paraPessoa(PapelPessoa.PROPRIETARIO) },
        historico = vistorias.map { v ->
            VistoriaHistorico(
                id = v.id.orEmpty(),
                tipo = tipoVistoriaDe(v.type),
                status = statusDe(v.status),
                data = formatarData(v.startedAt)
            )
        }
    )

    // ---------------- Pessoas ----------------

    suspend fun pessoasCadastradas(): List<PessoaVinculada> = chamar {
        todasAsPaginas { api.pessoas(page = it) }.map { it.paraPessoa(PapelPessoa.PROPRIETARIO) }
    }

    suspend fun proprietariosDoImovel(imovelId: String): List<PessoaVinculada> = chamar {
        api.proprietarios(imovelId).map { it.paraPessoa(PapelPessoa.PROPRIETARIO) }
    }

    // Com CNPJ, cadastra pessoa jurídica (o nome vira razão social e nome fantasia); senão, pessoa física.
    suspend fun cadastrarPessoa(pessoa: PessoaVinculada, cpf: String, cnpj: String): PessoaVinculada = chamar {
        val telefone = pessoa.telefone.ifBlank { null }
        val email = pessoa.email.ifBlank { null }
        val criada = if (cnpj.isNotBlank()) {
            api.cadastrarPessoaJuridica(PersonCompanyRequest(pessoa.nome, pessoa.nome, cnpj, telefone, email))
        } else {
            api.cadastrarPessoaFisica(IndividualRequest(pessoa.nome, cpf, telefone, email))
        }
        criada.paraPessoa(pessoa.papel)
    }

    // ---------------- Vistorias ----------------

    // Na saída, a API exige a última vistoria de entrada finalizada do imóvel e copia os ambientes dela.
    suspend fun iniciarVistoria(imovelId: String, tipo: TipoVistoria, observacoes: String): String = chamar {
        val entrada = if (tipo == TipoVistoria.SAIDA) {
            todasAsPaginas { api.vistorias(imovel = imovelId, status = STATUS_FINALIZADA, page = it) }
                .filter { it.type == TIPO_ENTRADA }
                .maxByOrNull { it.finishedAt.orEmpty() }
                ?.id
                ?: throw ApiErro("Para a vistoria de saída, o imóvel precisa de uma vistoria de entrada finalizada.")
        } else null

        val criada = api.iniciarVistoria(
            InspectionRequest(
                property = imovelId,
                type = if (tipo == TipoVistoria.ENTRADA) TIPO_ENTRADA else TIPO_SAIDA,
                entry = entrada,
                notes = observacoes.trim().ifBlank { null }
            )
        )
        criada.id ?: throw ApiErro("O servidor não devolveu a vistoria criada.")
    }

    suspend fun pessoasDaVistoria(vistoriaId: String): List<PessoaVinculada> = chamar {
        api.pessoasDaVistoria(vistoriaId).mapNotNull { p ->
            p.person?.paraPessoa(if (p.role == PAPEL_INQUILINO) PapelPessoa.INQUILINO else PapelPessoa.PROPRIETARIO)
        }
    }

    // Vincula as pessoas à vistoria; os proprietários também ficam vinculados ao imóvel.
    suspend fun vincularPessoas(vistoriaId: String, imovelId: String, pessoas: List<PessoaVinculada>) = chamar {
        pessoas.filter { it.id.isNotBlank() }.forEach { pessoa ->
            val papel = if (pessoa.papel == PapelPessoa.INQUILINO) PAPEL_INQUILINO else PAPEL_PROPRIETARIO
            api.vincularPessoa(vistoriaId, ParticipantRequest(pessoa.id, papel)).exigirSucesso()
            if (pessoa.papel == PapelPessoa.PROPRIETARIO) {
                api.vincularProprietario(imovelId, OwnerRequest(pessoa.id)).exigirSucesso()
            }
        }
    }

    suspend fun ambientes(vistoriaId: String): List<Ambiente> = chamar {
        api.vistoria(vistoriaId).rooms.orEmpty().map { it.paraAmbiente() }
    }

    suspend fun vistoriaCompleta(vistoriaId: String): VistoriaCompleta = chamar {
        coroutineScope {
            val detalhe: InspectionDetailResponse = api.vistoria(vistoriaId)
            val pessoas = async { pessoasDaVistoria(vistoriaId) }
            val imovel = detalhe.property?.let { id -> async { imovel(id) } }
            VistoriaCompleta(
                id = vistoriaId,
                imovel = imovel?.await(),
                pessoas = pessoas.await(),
                ambientes = detalhe.rooms.orEmpty().map { it.paraAmbiente() },
                finalizada = detalhe.status == STATUS_FINALIZADA,
                tipo = tipoVistoriaDe(detalhe.type),
                responsavel = detalhe.responsible?.name.orEmpty(),
                iniciadaEm = formatarDataHora(detalhe.startedAt),
                finalizadaEm = formatarDataHora(detalhe.finishedAt),
                observacoes = detalhe.notes.orEmpty()
            )
        }
    }

    suspend fun finalizarVistoria(vistoriaId: String) = chamar {
        api.finalizarVistoria(vistoriaId)
        Unit
    }

    // ---------------- Ambientes e itens ----------------

    suspend fun tiposDeAmbiente(): List<TipoDeAmbiente> = chamar {
        api.tiposDeAmbiente().mapNotNull { t ->
            val id = t.id ?: return@mapNotNull null
            TipoDeAmbiente(id, t.name.orEmpty())
        }
    }

    // Com tipo do catálogo, a API já cria os itens típicos. Sem tipo, criamos os itens padrão do app;
    // ao duplicar, copiamos os itens (e as avaliações) do ambiente escolhido.
    suspend fun adicionarAmbiente(
        vistoriaId: String,
        nome: String,
        tipoId: String?,
        observacoes: String,
        duplicarDe: Ambiente?
    ): Ambiente = chamar {
        val usarTipo = tipoId.takeIf { duplicarDe == null }
        val sala = api.adicionarAmbiente(vistoriaId, RoomRequest(nome.trim(), usarTipo, observacoes.trim().ifBlank { null }))
        val salaId = sala.id ?: throw ApiErro("O servidor não devolveu o ambiente criado.")

        val itensACriar = when {
            duplicarDe != null -> duplicarDe.itens
            usarTipo == null -> itensPadrao()
            else -> emptyList()
        }
        val criados = itensACriar.map { api.adicionarItem(salaId, it.paraRequest()) }
        sala.paraAmbiente().let { amb -> amb.copy(itens = amb.itens + criados.map { it.paraItem(0) }) }
    }

    // Item sem id da API (ou com id local) é criado; com id, é alterado.
    suspend fun salvarItem(ambienteId: String, item: ItemAmbiente, novo: Boolean): ItemAmbiente = chamar {
        val resposta = if (novo) api.adicionarItem(ambienteId, item.paraRequest())
        else api.alterarItem(item.id, item.paraEdicao())
        resposta.paraItem(item.fotos).copy(fotosIds = item.fotosIds)
    }

    suspend fun removerAmbiente(ambienteId: String) = chamar {
        api.removerAmbiente(ambienteId).exigirSucesso()
    }

    suspend fun removerItem(itemId: String) = chamar {
        api.removerItem(itemId).exigirSucesso()
    }

    // Envia uma foto (JPEG) como evidência do item e devolve o id da evidência.
    suspend fun enviarFoto(ambienteId: String, itemId: String, jpeg: ByteArray): String = chamar {
        val arquivo = MultipartBody.Part.createFormData(
            "file", "foto.jpg", jpeg.toRequestBody("image/jpeg".toMediaType())
        )
        api.enviarEvidencia(ambienteId, arquivo, itemId.toRequestBody("text/plain".toMediaType())).id.orEmpty()
    }

    // Envia a narração (M4A) como evidência do ambiente, sem item, e devolve o id da evidência.
    suspend fun enviarNarracao(ambienteId: String, audio: ByteArray): String = chamar {
        val arquivo = MultipartBody.Part.createFormData(
            "file", "narracao.m4a", audio.toRequestBody(TIPO_AUDIO.toMediaType())
        )
        api.enviarEvidencia(ambienteId, arquivo, null).id.orEmpty()
    }

    // Baixa a foto de uma evidência (para o laudo em PDF).
    suspend fun baixarFoto(evidenciaId: String): ByteArray = chamar {
        api.evidencia(evidenciaId).use { it.bytes() }
    }

    // ---------------- Conversões ----------------

    // A API devolve no máximo 50 registros por página: junta as páginas até completar o total.
    private suspend fun <T> todasAsPaginas(pagina: suspend (Int) -> PageResponse<T>): List<T> {
        val todos = mutableListOf<T>()
        var numero = 0
        while (numero < LIMITE_DE_PAGINAS) {
            val atual = pagina(numero)
            val itens = atual.items.orEmpty()
            todos += itens
            if (itens.isEmpty() || todos.size >= (atual.total ?: 0L)) break
            numero++
        }
        return todos
    }

    private fun retrofit2.Response<Unit>.exigirSucesso() {
        if (!isSuccessful) throw retrofit2.HttpException(this)
    }

    private fun RoomResponse.paraAmbiente(): Ambiente {
        val (audios, fotos) = evidences.orEmpty().filter { it.id != null }.partition { it.contentType == TIPO_AUDIO }
        val fotosPorItem = fotos.groupBy({ it.item }, { it.id.orEmpty() })
        return Ambiente(
            id = id.orEmpty(),
            nome = name.orEmpty(),
            itens = items.orEmpty().map { it.paraItem(fotosPorItem[it.id].orEmpty()) },
            observacoes = notes.orEmpty(),
            narracoes = audios.map { Narracao(it.id.orEmpty(), formatarDataHora(it.receivedAt), it.size ?: 0) }
        )
    }

    private fun ItemResponse.paraItem(fotos: List<String>): ItemAmbiente = paraItem(fotos.size).copy(fotosIds = fotos)

    private fun ItemResponse.paraItem(fotos: Int): ItemAmbiente {
        // Itens gravados antes do campo de cor na API guardavam a cor na primeira linha das observações
        val (corAntiga, obs) = if (color.isNullOrBlank()) separarCor(notes.orEmpty()) else "" to notes.orEmpty()
        return ItemAmbiente(
            id = id.orEmpty(),
            nome = name.orEmpty(),
            estado = EstadoItem.entries.firstOrNull { it.valorApi == condition },
            descricao = description.orEmpty(),
            material = material.orEmpty(),
            cor = color?.takeIf { it.isNotBlank() } ?: corAntiga,
            observacoes = obs,
            fotos = fotos
        )
    }

    private fun ItemAmbiente.paraRequest() = ItemRequest(
        name = nome.trim(),
        description = descricao.trim().ifBlank { null },
        condition = estado?.valorApi,
        material = material.trim().ifBlank { null },
        color = cor.trim().ifBlank { null },
        notes = observacoes.trim().ifBlank { null }
    )

    // Na edição (PATCH), null é "não mexer": o campo que o usuário apagou vai como texto vazio, que a API apaga.
    private fun ItemAmbiente.paraEdicao() = ItemRequest(
        name = nome.trim(),
        description = descricao.trim(),
        condition = estado?.valorApi,
        material = material.trim(),
        color = cor.trim(),
        notes = observacoes.trim()
    )

    private fun PersonSummaryResponse.paraPessoa(papel: PapelPessoa) = PessoaVinculada(
        id = id.orEmpty(),
        nome = name.orEmpty(),
        papel = papel,
        documento = documentoFormatado(type, document)
    )

    private fun PersonResponse.paraPessoa(papel: PapelPessoa) = PessoaVinculada(
        id = id.orEmpty(),
        nome = if (type == PESSOA_JURIDICA) tradeName ?: name.orEmpty() else name.orEmpty(),
        papel = papel,
        documento = documentoFormatado(type, document),
        telefone = phone.orEmpty(),
        email = email.orEmpty()
    )

    private companion object {
        const val PAPEL_PROPRIETARIO = "proprietario"
        const val PAPEL_INQUILINO = "inquilino"
        const val TIPO_ENTRADA = "entrada"
        const val TIPO_SAIDA = "saida"
        const val STATUS_FINALIZADA = "finalizada"
        const val PESSOA_JURIDICA = "pj"
        const val LIMITE_DE_PAGINAS = 40

        // Tipo que a API devolve para a narração em áudio (M4A)
        const val TIPO_AUDIO = "audio/mp4"
    }
}

// ---------------- Formatação ----------------

// Antes de a API ter o campo de cor, o app gravava a cor na primeira linha das observações ("Cor: Branco").
// Só a leitura continua, para os itens antigos.
private const val PREFIXO_COR = "Cor: "

internal fun separarCor(notas: String): Pair<String, String> {
    if (!notas.startsWith(PREFIXO_COR)) return "" to notas
    val primeira = notas.substringBefore("\n")
    val resto = if (notas.contains("\n")) notas.substringAfter("\n") else ""
    return primeira.removePrefix(PREFIXO_COR).trim() to resto
}

private fun statusDe(status: String?) =
    if (status == "finalizada") StatusVistoria.CONCLUIDA else StatusVistoria.EM_ANDAMENTO

private fun tipoVistoriaDe(tipo: String?) =
    if (tipo == "saida") TipoVistoria.SAIDA else TipoVistoria.ENTRADA

private fun nomeDoImovel(imovel: PropertyResponse): String {
    imovel.description?.takeIf { it.isNotBlank() }?.let { return it }
    val a = imovel.address ?: return "Imóvel"
    val complemento = a.complement?.takeIf { it.isNotBlank() }?.let { ", $it" }.orEmpty()
    return "${a.street.orEmpty()}, ${a.number.orEmpty()}$complemento"
}

private fun enderecoCurto(a: AddressResponse) = "${a.street.orEmpty()}, ${a.number.orEmpty()} — ${a.city.orEmpty()}/${a.state.orEmpty()}"

private fun enderecoCompleto(a: AddressResponse) =
    "${a.street.orEmpty()}, ${a.number.orEmpty()} — ${a.district.orEmpty()}, ${a.city.orEmpty()}/${a.state.orEmpty()}"

// "2026-07-18T13:45:00Z" -> "18 jul 2026" (no fuso do aparelho: 23h em Brasília ainda é o mesmo dia)
internal fun formatarData(instante: String?, fuso: TimeZone = TimeZone.getDefault()): String =
    formatarInstante(instante, "dd MMM yyyy", fuso).replace(".", "")

// "2026-07-18T13:45:00Z" -> "18/07/2026 10:45" (no fuso do aparelho)
internal fun formatarDataHora(instante: String?, fuso: TimeZone = TimeZone.getDefault()): String =
    formatarInstante(instante, "dd/MM/yyyy HH:mm", fuso)

// A API devolve instantes em UTC (ISO 8601, terminados em Z). Lê até os segundos e formata no fuso pedido.
private fun formatarInstante(instante: String?, padrao: String, fuso: TimeZone): String {
    if (instante.isNullOrBlank() || instante.length < 19) return ""
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val data = entrada.parse(instante.take(19)) ?: return ""
        SimpleDateFormat(padrao, Locale.forLanguageTag("pt-BR")).apply { timeZone = fuso }.format(data)
    } catch (_: Exception) {
        ""
    }
}

private fun documentoFormatado(tipo: String?, documento: String?): String {
    val doc = documento.orEmpty()
    if (doc.isBlank()) return ""
    return if (tipo == "pj") "CNPJ ${formatarCnpj(doc)}" else "CPF ${formatarCpf(doc)}"
}

internal fun formatarCpf(cpf: String): String {
    val d = cpf.filter { it.isDigit() }
    if (d.length != 11) return cpf
    return "${d.substring(0, 3)}.${d.substring(3, 6)}.${d.substring(6, 9)}-${d.substring(9)}"
}

internal fun formatarCnpj(cnpj: String): String {
    val d = cnpj.filter { it.isDigit() }
    if (d.length != 14) return cnpj
    return "${d.substring(0, 2)}.${d.substring(2, 5)}.${d.substring(5, 8)}/${d.substring(8, 12)}-${d.substring(12)}"
}
