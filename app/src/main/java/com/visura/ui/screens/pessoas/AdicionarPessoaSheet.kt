package com.visura.ui.screens.pessoas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.screens.createaccount.LabeledField
import com.visura.ui.screens.imoveis.PapelPessoa
import com.visura.ui.screens.imoveis.PessoaVinculada
import com.visura.ui.theme.BackgroundLavender
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue

@Composable
fun AdicionarPessoaSheet(
    papel: PapelPessoa,
    onClose: () -> Unit,
    onSave: (PessoaVinculada) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cnpj by remember { mutableStateOf("") }

    val emailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val podeSalvar = nome.isNotBlank() && cpf.isNotBlank() &&
            telefone.isNotBlank() && emailValido

    val explicacao = when (papel) {
        PapelPessoa.PROPRIETARIO ->
            "Vínculo inferido automaticamente: ainda não há proprietário cadastrado, então esta pessoa será o proprietário."
        PapelPessoa.INQUILINO ->
            "Vínculo inferido automaticamente: já existe um proprietário cadastrado, então esta pessoa será o inquilino."
    }

    PainelInferior(onClose = onClose) {
        Text(
            "Adicionar ${papel.rotulo.lowercase()}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundLavender)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = NavyBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(explicacao, fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Tipo de vínculo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(LightBlueBg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = NavyBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(papel.rotulo, fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        LabeledField("Nome completo *", nome, { nome = it }, placeholder = "Nome da pessoa")
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                LabeledField(
                    "CPF *", cpf, { cpf = it },
                    placeholder = "000.000.000-00",
                    keyboardType = KeyboardType.Number
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                LabeledField(
                    "Telefone *", telefone, { telefone = it },
                    placeholder = "(00) 00000-0000",
                    keyboardType = KeyboardType.Phone
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LabeledField(
            "E-mail *", email, { email = it },
            placeholder = "email@exemplo.com",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
        LabeledField(
            "CNPJ (opcional)", cnpj, { cnpj = it },
            placeholder = "Se aplicável",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSave(
                    PessoaVinculada(
                        nome = nome.trim(),
                        papel = papel,
                        documento = if (cnpj.isNotBlank()) "CNPJ ${cnpj.trim()}" else "CPF ${cpf.trim()}",
                        telefone = telefone.trim(),
                        email = email.trim()
                    )
                )
            },
            enabled = podeSalvar,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Salvar pessoa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
