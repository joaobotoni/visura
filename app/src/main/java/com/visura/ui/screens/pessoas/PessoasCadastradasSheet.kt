package com.visura.ui.screens.pessoas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.screens.imoveis.PapelPessoa
import com.visura.ui.screens.imoveis.PessoaVinculada
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

// SIMULAÇÃO — será substituída pelas pessoas realmente cadastradas pelo vistoriador.
private val pessoasCadastradasDeExemplo = listOf(
    PessoaVinculada("Carla Souza Lima", PapelPessoa.PROPRIETARIO, "CPF 022.884.610-91"),
    PessoaVinculada("Rafael Nunes Costa", PapelPessoa.INQUILINO, "CPF 509.221.360-77"),
    PessoaVinculada("Beatriz Lima", PapelPessoa.PROPRIETARIO, "CPF 344.902.150-22"),
    PessoaVinculada("Empresa Delta", PapelPessoa.PROPRIETARIO, "CNPJ 19.204.885/0001-30")
)

@Composable
fun PessoasCadastradasSheet(
    jaVinculadas: List<PessoaVinculada>,
    onClose: () -> Unit,
    onPessoaClick: (PessoaVinculada) -> Unit,
    onCadastrarNovaClick: () -> Unit
) {
    var busca by remember { mutableStateOf("") }

    // Quem já está na vistoria não aparece de novo; a busca olha nome e documento
    val resultados = remember(busca, jaVinculadas) {
        val nomesVinculados = jaVinculadas.map { it.nome }.toSet()
        pessoasCadastradasDeExemplo
            .filter { it.nome !in nomesVinculados }
            .filter {
                busca.isBlank() ||
                        it.nome.contains(busca.trim(), ignoreCase = true) ||
                        it.documento.contains(busca.trim())
            }
    }

    PainelInferior(onClose = onClose) {
        Text("Pessoas já cadastradas", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            placeholder = { Text("Buscar por nome ou CPF") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextGray)
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightBlueBg,
                unfocusedContainerColor = LightBlueBg,
                focusedBorderColor = NavyBlue,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (resultados.isEmpty()) {
            Text(
                "Nenhuma pessoa encontrada.",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        resultados.forEach { pessoa ->
            PessoaCard(
                pessoa = pessoa,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPessoaClick(pessoa) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = onCadastrarNovaClick,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, NavyBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cadastrar nova pessoa", color = NavyBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
