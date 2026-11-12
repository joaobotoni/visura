package com.visura.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.LogoutRed
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

@Composable
fun PerfilContent(
    nomeUsuario: String,
    email: String,
    cpf: String,
    cnpj: String,
    vistorias: List<Vistoria>,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {}
) {
    val totais = vistorias.size
    val concluidas = vistorias.count { it.status == StatusVistoria.CONCLUIDA }

    val iniciais = nomeUsuario
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NavyBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(iniciais, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(nomeUsuario, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(email, fontSize = 13.sp, color = TextGray)

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard("$totais", "Vistorias totais", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            StatCard("$concluidas", "Concluídas", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "DADOS PESSOAIS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PerfilDocField(label = "CPF", valor = cpf, icon = Icons.Filled.Badge)
        Spacer(modifier = Modifier.height(10.dp))
        PerfilDocField(label = "CNPJ", valor = cnpj, icon = Icons.Filled.Business)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLogoutClick() }
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = LogoutRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da conta", color = LogoutRed, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PerfilDocField(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .cardBorder()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(valor, color = TextGray, fontSize = 13.sp)
    }
}