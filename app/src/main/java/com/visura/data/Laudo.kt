package com.visura.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.visura.model.EstadoItem
import com.visura.model.ItemAmbiente
import com.visura.model.StatusAmbiente
import com.visura.model.TipoVistoria
import com.visura.model.VistoriaCompleta

// Laudo da vistoria em PDF (A4), montado no aparelho a partir dos dados da API, com as fotos de cada item.
// O arquivo fica no cache (pasta "laudos", exposta pelo FileProvider) e é substituído a cada geração.
suspend fun gerarLaudo(
    context: Context,
    vistoria: VistoriaCompleta,
    baixarFoto: suspend (String) -> ByteArray
): File {
    val fotos = baixarFotos(vistoria, baixarFoto)
    return withContext(Dispatchers.IO) {
        val documento = PdfDocument()
        try {
            LaudoPdf(documento).apply { escrever(vistoria, fotos) }.terminar()
            val pasta = File(context.cacheDir, "laudos").apply { mkdirs() }
            val arquivo = File(pasta, "laudo-vistoria-${vistoria.id.take(8)}.pdf")
            arquivo.outputStream().use { documento.writeTo(it) }
            arquivo
        } catch (e: java.io.IOException) {
            throw ApiErro("Não foi possível gravar o laudo: ${e.message ?: "erro de arquivo"}")
        } finally {
            documento.close()
            fotos.values.forEach { it.recycle() }
        }
    }
}

// Baixa e reduz as fotos antes de desenhar; uma foto que falhar fica de fora (o laudo avisa no item).
private suspend fun baixarFotos(vistoria: VistoriaCompleta, baixarFoto: suspend (String) -> ByteArray): Map<String, Bitmap> {
    val ids = vistoria.ambientes.flatMap { it.itens }.flatMap { it.fotosIds }
    val fotos = mutableMapOf<String, Bitmap>()
    for (id in ids) {
        val bytes = try {
            baixarFoto(id)
        } catch (_: ApiErro) {
            continue
        }
        withContext(Dispatchers.Default) { reduzir(bytes) }?.let { fotos[id] = it }
    }
    return fotos
}

private const val LADO_FOTO = 480
private const val ESPACO_FOTOS = 8f

private fun reduzir(bytes: ByteArray): Bitmap? {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
    if (limites.outWidth <= 0) return null
    var amostra = 1
    while (maxOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= LADO_FOTO) amostra *= 2
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = amostra })
        ?: return null
    val escala = LADO_FOTO.toFloat() / maxOf(bitmap.width, bitmap.height)
    if (escala >= 1f) return bitmap
    return bitmap.scale((bitmap.width * escala).toInt(), (bitmap.height * escala).toInt())
        .also { if (it != bitmap) bitmap.recycle() }
}

// Desenha o conteúdo de cima para baixo, abrindo página nova quando o próximo bloco não cabe.
private class LaudoPdf(private val documento: PdfDocument) {

    private val largura = 595
    private val altura = 842
    private val margem = 40f
    private val util = largura - 2 * margem
    private val rodape = 30f
    private val ladoDaFoto = (util - 10f - 2 * ESPACO_FOTOS) / 3

    private val azul = Color.rgb(0x1B, 0x2A, 0x4E)
    private val cinza = Color.rgb(0x6B, 0x6F, 0x7B)
    private val linha = Color.rgb(0xDD, 0xDF, 0xE6)

    private val titulo = texto(20f, negrito = true, cor = azul)
    private val subtitulo = texto(13f, negrito = true, cor = azul)
    private val forte = texto(10.5f, negrito = true)
    private val normal = texto(10f)
    private val discreto = texto(9f, cor = cinza)

    private val geradoEm = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())

    private var numero = 0
    private lateinit var pagina: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var y = 0f

    init {
        novaPagina()
    }

    fun escrever(v: VistoriaCompleta, fotos: Map<String, Bitmap>) {
        val tipo = if (v.tipo == TipoVistoria.SAIDA) "saída" else "entrada"
        paragrafo("Laudo de vistoria de $tipo", titulo, depois = 4f)
        paragrafo(v.imovel?.nome ?: "Imóvel", subtitulo, depois = 2f)
        v.imovel?.let {
            paragrafo(it.endereco, normal, depois = 2f)
            paragrafo("${it.tipo} · ${it.finalidade}", discreto)
        }
        separador()

        campo("Situação", if (v.finalizada) "Concluída" else "Em andamento")
        if (v.iniciadaEm.isNotBlank()) campo("Início", v.iniciadaEm)
        if (v.finalizadaEm.isNotBlank()) campo("Conclusão", v.finalizadaEm)
        if (v.responsavel.isNotBlank()) campo("Responsável", v.responsavel)
        if (v.observacoes.isNotBlank()) campo("Observações", v.observacoes)

        if (v.pessoas.isNotEmpty()) {
            espaco(8f)
            paragrafo("Pessoas vinculadas", subtitulo, depois = 4f)
            v.pessoas.forEach { p ->
                val doc = p.documento.takeIf { it.isNotBlank() }?.let { " — $it" }.orEmpty()
                paragrafo("${p.papel.rotulo}: ${p.nome}$doc", normal, depois = 2f)
            }
        }

        val itens = v.ambientes.flatMap { it.itens }
        val problemas = itens.count { it.estado == EstadoItem.RUIM || it.estado == EstadoItem.DANIFICADO }
        espaco(8f)
        paragrafo("Resumo", subtitulo, depois = 4f)
        paragrafo(
            "${v.ambientes.size} ambientes, ${itens.count { it.estado != null }} de ${itens.size} itens avaliados, " +
                "$problemas com problema (ruim ou danificado), ${itens.sumOf { it.fotos }} fotos.",
            normal
        )

        v.ambientes.forEach { ambiente ->
            espaco(10f)
            cabecalhoAmbiente(ambiente.nome, ambiente.status)
            if (ambiente.observacoes.isNotBlank()) paragrafo(ambiente.observacoes, discreto, depois = 4f)
            if (ambiente.itens.isEmpty()) paragrafo("Sem itens.", discreto)
            ambiente.itens.forEach { item(it, fotos) }
        }

        assinaturas(v)
    }

    fun terminar() {
        fecharPagina()
    }

    private fun item(item: ItemAmbiente, fotos: Map<String, Bitmap>) {
        val estado = item.estado?.rotulo ?: "Não avaliado"
        val desenhaveis = item.fotosIds.mapNotNull { fotos[it] }
        // O nome do item não fica sozinho no fim da página, longe das fotos dele
        caber(if (desenhaveis.isEmpty()) 30f else 30f + ladoDaFoto + ESPACO_FOTOS)
        paragrafo("• ${item.nome} — $estado", forte, depois = 2f)
        val detalhes = listOfNotNull(
            item.descricao.takeIf { it.isNotBlank() },
            item.material.takeIf { it.isNotBlank() }?.let { "Material: $it" },
            item.cor.takeIf { it.isNotBlank() }?.let { "Cor: $it" }
        )
        if (detalhes.isNotEmpty()) paragrafo(detalhes.joinToString(" · "), discreto, recuo = 10f, depois = 2f)
        if (item.observacoes.isNotBlank()) paragrafo(item.observacoes, normal, recuo = 10f, depois = 2f)

        if (desenhaveis.size < item.fotosIds.size) {
            paragrafo("${item.fotosIds.size - desenhaveis.size} foto(s) não puderam ser baixadas.", discreto, recuo = 10f)
        }
        grade(desenhaveis)
        espaco(4f)
    }

    // Fotos em linhas de três, no tamanho da célula, mantendo a proporção
    private fun grade(fotos: List<Bitmap>) {
        val espacamento = ESPACO_FOTOS
        val lado = ladoDaFoto
        fotos.chunked(3).forEach { linhaDeFotos ->
            caber(lado + espacamento)
            linhaDeFotos.forEachIndexed { i, foto ->
                val x = margem + 10f + i * (lado + espacamento)
                val escala = minOf(lado / foto.width, lado / foto.height)
                val w = foto.width * escala
                val h = foto.height * escala
                val destino = RectF(x + (lado - w) / 2, y + (lado - h) / 2, x + (lado + w) / 2, y + (lado + h) / 2)
                canvas.drawBitmap(foto, null, destino, Paint(Paint.FILTER_BITMAP_FLAG))
            }
            y += lado + espacamento
        }
    }

    private fun cabecalhoAmbiente(nome: String, status: StatusAmbiente) {
        caber(60f)
        val fundo = Paint().apply { color = Color.rgb(0xEE, 0xF0, 0xF8) }
        canvas.drawRoundRect(RectF(margem, y, margem + util, y + 24f), 6f, 6f, fundo)
        val situacao = when (status) {
            StatusAmbiente.CONCLUIDO -> "Concluído"
            StatusAmbiente.EM_ANDAMENTO -> "Em andamento"
            StatusAmbiente.PENDENTE -> "Pendente"
        }
        canvas.drawText(nome, margem + 8f, y + 16f, subtitulo)
        canvas.drawText(situacao, margem + util - 8f - discreto.measureText(situacao), y + 16f, discreto)
        y += 32f
    }

    private fun assinaturas(v: VistoriaCompleta) {
        val nomes = listOfNotNull(v.responsavel.takeIf { it.isNotBlank() }?.let { "$it (responsável)" }) +
            v.pessoas.map { "${it.nome} (${it.papel.rotulo.lowercase()})" }
        if (nomes.isEmpty()) return
        espaco(16f)
        caber(40f)
        paragrafo("Assinaturas", subtitulo, depois = 8f)
        nomes.forEach { nome ->
            caber(48f)
            y += 28f
            val traco = Paint().apply { color = cinza; strokeWidth = 0.8f }
            canvas.drawLine(margem, y, margem + util * 0.6f, y, traco)
            y += 4f
            paragrafo(nome, discreto, depois = 6f)
        }
    }

    private fun campo(rotulo: String, valor: String) {
        paragrafo("$rotulo: $valor", normal, depois = 2f)
    }

    private fun separador() {
        espaco(6f)
        canvas.drawLine(margem, y, margem + util, y, Paint().apply { color = linha; strokeWidth = 1f })
        espaco(10f)
    }

    private fun espaco(altura: Float) {
        y += altura
    }

    // Texto com quebra de linha automática; um parágrafo longo continua na página seguinte.
    private fun paragrafo(texto: String, estilo: TextPaint, recuo: Float = 0f, depois: Float = 6f) {
        val layout = StaticLayout.Builder
            .obtain(texto, 0, texto.length, estilo, (util - recuo).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .build()
        for (linhaAtual in 0 until layout.lineCount) {
            val alturaDaLinha = (layout.getLineBottom(linhaAtual) - layout.getLineTop(linhaAtual)).toFloat()
            caber(alturaDaLinha)
            val base = y + layout.getLineBaseline(linhaAtual) - layout.getLineTop(linhaAtual)
            val inicio = layout.getLineStart(linhaAtual)
            val fim = layout.getLineEnd(linhaAtual)
            canvas.drawText(texto.substring(inicio, fim).trimEnd('\n'), margem + recuo, base, estilo)
            y += alturaDaLinha
        }
        y += depois
    }

    // Abre página nova se o bloco não couber no que sobra desta
    private fun caber(altura: Float) {
        if (y + altura > this.altura - margem - rodape) {
            fecharPagina()
            novaPagina()
        }
    }

    private fun novaPagina() {
        numero++
        pagina = documento.startPage(PdfDocument.PageInfo.Builder(largura, altura, numero).create())
        canvas = pagina.canvas
        y = margem
    }

    private fun fecharPagina() {
        val base = altura - margem / 2
        canvas.drawText("Laudo gerado pelo Visura em $geradoEm", margem, base, discreto)
        val pag = "Página $numero"
        canvas.drawText(pag, margem + util - discreto.measureText(pag), base, discreto)
        documento.finishPage(pagina)
    }

    private fun texto(tamanho: Float, negrito: Boolean = false, cor: Int = Color.rgb(0x1F, 0x21, 0x27)) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = tamanho
            color = cor
            typeface = if (negrito) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        }
}
