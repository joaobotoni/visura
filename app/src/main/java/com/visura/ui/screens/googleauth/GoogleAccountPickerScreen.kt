package com.visura.ui.screens.googleauth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

data class GoogleAccount(
    val nome: String,
    val email: String
)
/*SIMULAÇÃO DE CONTAS - SERÁ RETIRADO DEPOIS   */
private val contasFake = listOf(
    GoogleAccount("Marcos Vinícius Andrade", "marcos.andrade@gmail.com"),
    GoogleAccount("Marcos Andrade (Trabalho)", "m.andrade.vistoriapro@gmail.com")
)

@Composable
fun GoogleAccountPickerScreen(
    onBackClick: () -> Unit = {},
    onAccountSelected: () -> Unit = {}
) {
    var contaSelecionada by remember { mutableStateOf<GoogleAccount?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.padding(bottom = 4.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }

        Text("Escolha uma conta", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "para continuar no Visura",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        contasFake.forEach { conta ->
            GoogleAccountItem(
                conta = conta,
                selecionada = contaSelecionada == conta,
                onClick = { contaSelecionada = conta }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { /* placeholder: adicionar outra conta */ }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, BorderGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = TextGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Usar outra conta", fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAccountSelected,
            enabled = contaSelecionada != null,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun GoogleAccountItem(
    conta: GoogleAccount,
    selecionada: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selecionada) Color(0xFFEFF3FC) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selecionada) NavyBlue else BorderGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(conta.nome, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(conta.email, fontSize = 13.sp, color = TextGray)
        }
    }
}