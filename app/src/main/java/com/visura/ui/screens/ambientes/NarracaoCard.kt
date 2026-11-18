package com.visura.ui.screens.ambientes

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.visura.data.EnviosDeNarracao
import com.visura.ui.navigation.Gravacao
import com.visura.ui.theme.AlertaErroIcone
import com.visura.ui.theme.AlertaErroText
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray
import kotlinx.coroutines.delay

// Narração do ambiente: o vistoriador fala o que vê (item, material, cor, estado) enquanto anda pelo cômodo.
// Pode pausar, continuar e abrir os itens sem parar a gravação. Ao concluir, o áudio vai para a fila de envio.
@Composable
fun NarracaoCard(
    ambienteId: String,
    gravacao: Gravacao?,
    enviadas: Int,
    envios: EnviosDeNarracao,
    onGravar: () -> Unit,
    onPausar: () -> Unit,
    onContinuar: () -> Unit,
    onConcluir: () -> Unit,
    onDescartar: () -> Unit
) {
    val contexto = LocalContext.current
    var semPermissao by remember { mutableStateOf(false) }
    val pedirMicrofone = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        semPermissao = !concedida
        if (concedida) onGravar()
    }
    val gravandoAqui = gravacao?.ambienteId == ambienteId

    // Com a tela apagada, o Android corta o microfone do app: enquanto grava, a tela fica acesa
    val view = LocalView.current
    DisposableEffect(gravandoAqui) {
        view.keepScreenOn = gravandoAqui
        onDispose { view.keepScreenOn = false }
    }

    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (gravandoAqui) LightBlueBg else Color.White, forma)
            .border(if (gravandoAqui) 2.dp else 1.dp, if (gravandoAqui) NavyBlue else BorderGray, forma)
            .padding(14.dp)
            .testTag("narracao")
    ) {
        Text("Narração do ambiente", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))

        when {
            gravandoAqui -> Gravando(gravacao!!, onPausar, onContinuar, onConcluir, onDescartar)
            gravacao != null -> Text(
                "Gravando a narração de ${gravacao.ambienteNome}. Conclua aquela antes de gravar esta.",
                fontSize = 12.sp,
                color = TextGray
            )
            else -> {
                Text(
                    "Fale o que vê enquanto anda pelo ambiente: cada item, o material, a cor e o estado.",
                    fontSize = 12.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val permitido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        if (permitido) onGravar() else pedirMicrofone.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gravar narração", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                if (semPermissao) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Sem acesso ao microfone. Permita nas configurações do Android para gravar.",
                        fontSize = 12.sp,
                        color = AlertaErroText
                    )
                }
            }
        }

        Situacao(enviadas, envios)
    }
}

@Composable
private fun Gravando(
    gravacao: Gravacao,
    onPausar: () -> Unit,
    onContinuar: () -> Unit,
    onConcluir: () -> Unit,
    onDescartar: () -> Unit
) {
    var agora by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(gravacao.pausada) {
        while (!gravacao.pausada) {
            agora = SystemClock.elapsedRealtime()
            delay(500)
        }
    }
    val decorrido = gravacao.gravadoMs + if (gravacao.pausada) 0 else (agora - gravacao.retomadaEm).coerceAtLeast(0)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (gravacao.pausada) TextGray else AlertaErroIcone, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (gravacao.pausada) "Pausada · ${cronometro(decorrido)}" else "Gravando · ${cronometro(decorrido)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("cronometro")
        )
    }
    Text("Você pode abrir os itens e tirar fotos sem parar a gravação.", fontSize = 12.sp, color = TextGray)
    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = if (gravacao.pausada) onContinuar else onPausar,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(if (gravacao.pausada) Icons.Filled.PlayArrow else Icons.Filled.Pause, contentDescription = null, tint = NavyBlue)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (gravacao.pausada) "Continuar" else "Pausar", color = NavyBlue)
        }
        Button(
            onClick = onConcluir,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Concluir")
        }
    }
    TextButton(onClick = onDescartar, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.Delete, contentDescription = null, tint = AlertaErroIcone, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Descartar gravação", color = AlertaErroIcone, fontSize = 12.sp)
    }
}

@Composable
private fun Situacao(enviadas: Int, envios: EnviosDeNarracao) {
    if (enviadas == 0 && envios.aguardando == 0 && envios.recusadas.isEmpty()) return
    Spacer(modifier = Modifier.height(8.dp))
    if (enviadas > 0) {
        Text(
            if (enviadas == 1) "1 narração enviada" else "$enviadas narrações enviadas",
            fontSize = 12.sp,
            color = TextGray
        )
    }
    if (envios.aguardando > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (envios.aguardando == 1) "1 aguardando envio" else "${envios.aguardando} aguardando envio",
                fontSize = 12.sp,
                color = NavyBlue
            )
        }
    }
    envios.recusadas.distinct().forEach { motivo ->
        Text("Narração não enviada: $motivo", fontSize = 12.sp, color = AlertaErroText)
    }
}

// mm:ss (ou h:mm:ss numa narração muito longa)
internal fun cronometro(ms: Long): String {
    val segundos = ms / 1000
    val h = segundos / 3600
    val m = (segundos % 3600) / 60
    val s = segundos % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
