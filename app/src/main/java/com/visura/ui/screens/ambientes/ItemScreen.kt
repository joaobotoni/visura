package com.visura.ui.screens.ambientes

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.visura.data.miniaturaDaFoto
import com.visura.data.novaFotoDaCamera
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.screens.createaccount.LabeledField
import com.visura.ui.theme.BackgroundLavender
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray
import com.visura.model.ItemAmbiente

enum class ModoItem { NOVO, AVALIAR, EDITAR }

// Uma tela para três usos:
//  NOVO    — "Novo item — Sala" (item personalizado)
//  AVALIAR — "Portas — Sala" (estado, material, cor, observações e fotos)
//  EDITAR  — "Editar item — Sala" (como o NOVO, aberto pelo menu de segurar o item)
@Composable
fun ItemScreen(
    modo: ModoItem,
    nomeDoAmbiente: String,
    itemInicial: ItemAmbiente = ItemAmbiente(nome = ""),
    onBackClick: () -> Unit = {},
    // O segundo parâmetro são as fotos tiradas antes de o item existir na API (modo NOVO):
    // o NavGraph cria o item e depois envia cada uma.
    onSaveClick: (ItemAmbiente, List<Uri>) -> Unit = { _, _ -> },
    // Item que já existe na API: recebe a foto e avisa pelo segundo parâmetro quando o envio deu certo.
    // null no modo NOVO: as fotos ficam guardadas na tela até salvar.
    onFotoEscolhida: ((Uri, () -> Unit) -> Unit)? = null,
    // Miniatura de uma foto já enviada, pelo id da evidência (null se não der para baixar)
    carregarFoto: suspend (String) -> ImageBitmap? = { null }
) {
    // rememberSaveable: abrir a câmera pode fazer o Android recriar a tela, e o que foi digitado não pode se perder
    var nome by rememberSaveable { mutableStateOf(itemInicial.nome) }
    var descricao by rememberSaveable { mutableStateOf(itemInicial.descricao) }
    var material by rememberSaveable { mutableStateOf(itemInicial.material) }
    var cor by rememberSaveable { mutableStateOf(itemInicial.cor) }
    var estado by rememberSaveable { mutableStateOf(itemInicial.estado) }
    var observacoes by rememberSaveable { mutableStateOf(itemInicial.observacoes) }
    var fotos by rememberSaveable { mutableIntStateOf(itemInicial.fotos) }
    var fotosPendentes by rememberSaveable { mutableStateOf(listOf<Uri>()) }
    var escolhendoOrigem by remember { mutableStateOf(false) }
    var fotoDaCamera by rememberSaveable { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val editaNome = modo != ModoItem.AVALIAR

    // Foto nova: envia já (item salvo) ou guarda até salvar (item novo)
    fun usarFoto(uri: Uri) {
        val enviar = onFotoEscolhida
        if (enviar != null) enviar(uri) { fotos++ } else fotosPendentes = fotosPendentes + uri
    }

    val escolherFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) usarFoto(uri)
    }
    val tirarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tirada ->
        val uri = fotoDaCamera
        if (tirada && uri != null) usarFoto(uri)
        fotoDaCamera = null
    }

    if (escolhendoOrigem) {
        AlertDialog(
            onDismissRequest = { escolhendoOrigem = false },
            title = { Text("Adicionar foto") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Tirar foto") },
                        leadingContent = { Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = NavyBlue) },
                        modifier = Modifier.clickableOpcao {
                            escolhendoOrigem = false
                            val uri = novaFotoDaCamera(context)
                            fotoDaCamera = uri
                            try {
                                tirarFoto.launch(uri)
                            } catch (_: ActivityNotFoundException) {
                                fotoDaCamera = null
                                Toast.makeText(context, "Nenhum app de câmera neste aparelho. Escolha da galeria.", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Escolher da galeria") },
                        leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = NavyBlue) },
                        modifier = Modifier.clickableOpcao {
                            escolhendoOrigem = false
                            escolherFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { escolhendoOrigem = false }) { Text("Cancelar") } }
        )
    }

    val (titulo, textoBotao) = when (modo) {
        ModoItem.NOVO -> "Novo item — $nomeDoAmbiente" to "Adicionar item"
        ModoItem.AVALIAR -> "${itemInicial.nome} — $nomeDoAmbiente" to "Salvar item"
        ModoItem.EDITAR -> "Editar item — $nomeDoAmbiente" to "Salvar alterações"
    }

    EtapaLayout(
        titulo = titulo,
        onBackClick = onBackClick,
        rodape = {
            BotaoPrincipal(
                texto = textoBotao,
                enabled = nome.isNotBlank(),
                onClick = {
                    onSaveClick(
                        itemInicial.copy(
                            nome = nome.trim(),
                            descricao = descricao.trim(),
                            material = material.trim(),
                            cor = cor.trim(),
                            estado = estado,
                            observacoes = observacoes.trim(),
                            fotos = fotos
                        ),
                        fotosPendentes
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            if (editaNome) {
                Text(
                    "Descreva o item que deseja avaliar neste ambiente.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(18.dp))

                CampoNomeDoItem(nome) { nome = it }

                Spacer(modifier = Modifier.height(12.dp))
                LabeledField("Descrição (opcional)", descricao, { descricao = it }, placeholder = "Ex.: localização, uso, particularidade")
                Spacer(modifier = Modifier.height(18.dp))
            }

            Rotulo("Estado atual")
            Spacer(modifier = Modifier.height(8.dp))
            EstadoSelector(estado = estado, onEstadoChange = { estado = it })

            Spacer(modifier = Modifier.height(18.dp))

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField("Material", material, { material = it }, placeholder = "Ex.: MDF")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    LabeledField("Cor", cor, { cor = it }, placeholder = "Ex.: Branco")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Rotulo("Observações")
            Spacer(modifier = Modifier.height(8.dp))
            CampoMultilinha(observacoes, { observacoes = it }, "Descreva problemas encontrados ou informações relevantes...")

            Spacer(modifier = Modifier.height(18.dp))

            Rotulo("Evidências fotográficas")
            Spacer(modifier = Modifier.height(8.dp))
            EvidenciasGrid(
                fotos = itemInicial.fotosIds.map { Evidencia.Enviada(it) } + fotosPendentes.map { Evidencia.Pendente(it) },
                miniatura = { foto ->
                    when (foto) {
                        is Evidencia.Enviada -> carregarFoto(foto.id)
                        is Evidencia.Pendente -> miniaturaDaFoto(context, foto.uri)
                    }
                },
                onAdicionarFoto = { escolhendoOrigem = true }
            )
            if (fotosPendentes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "As fotos serão enviadas quando você salvar o item.",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Campo do nome com o ícone do item à esquerda (genérico quando o nome não é reconhecido)
@Composable
private fun CampoNomeDoItem(nome: String, onNomeChange: (String) -> Unit) {
    Text("Nome do item *", fontSize = 13.sp, color = TextGray)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = nome,
        onValueChange = onNomeChange,
        placeholder = { Text("Ex.: Aquário embutido") },
        singleLine = true,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(BackgroundLavender),
                contentAlignment = Alignment.Center
            ) {
                val icone = iconeDoItem(nome)
                Icon(
                    icone ?: Icons.Filled.Category,
                    contentDescription = null,
                    tint = if (icone != null) NavyBlue else TextGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = NavyBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
    if (nome.isNotBlank() && iconeDoItem(nome) == null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Não encontramos um ícone específico — usaremos um ícone genérico para este item.",
            fontSize = 12.sp,
            color = TextGray
        )
    }
}

private fun Modifier.clickableOpcao(onClick: () -> Unit): Modifier =
    clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
