package com.visura.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.LightBlueBg
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.StatusGreenBg
import com.visura.ui.theme.StatusGreenText
import com.visura.ui.theme.StatusOrangeBg
import com.visura.ui.theme.StatusOrangeText
import com.visura.ui.theme.TextGray
import java.util.Calendar

private fun saudacaoPorHorario(): String {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hora) {
        in 5..11 -> "Bom dia,"
        in 12..17 -> "Boa tarde,"
        else -> "Boa noite,"
    }
}

@Composable
fun HomeScreen(
    nomeUsuario: String = "Marcos Andrade",
    email: String = "marcos.andrade@vistoriapro.com",
    cpf: String = "385.201.740-06",
    cnpj: String = "28.114.902/0001-45",
    vistorias: List<Vistoria> = vistoriasExemplo,
    onNovoImovelClick: () -> Unit = {},
    onVistoriaClick: (Vistoria) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var abaSelecionada by remember { mutableStateOf(0) } // 0 = Home, 1 = Perfil

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = abaSelecionada == 0,
                    onClick = { abaSelecionada = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = NavyBlue,
                        indicatorColor = LightBlueBg
                    )
                )
                NavigationBarItem(
                    selected = abaSelecionada == 1,
                    onClick = { abaSelecionada = 1 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyBlue,
                        selectedTextColor = NavyBlue,
                        indicatorColor = LightBlueBg
                    )
                )
            }
        },
        floatingActionButton = {
            if (abaSelecionada == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNovoImovelClick,
                    containerColor = NavyBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Novo imóvel", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        when (abaSelecionada) {
            0 -> HomeContent(
                nomeUsuario = nomeUsuario,
                vistorias = vistorias,
                modifier = Modifier.padding(padding),
                onAvatarClick = { abaSelecionada = 1 },
                onVistoriaClick = onVistoriaClick
            )
            else -> PerfilContent(
                nomeUsuario = nomeUsuario,
                email = email,
                cpf = cpf,
                cnpj = cnpj,
                vistorias = vistorias,
                modifier = Modifier.padding(padding),
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun HomeContent(
    nomeUsuario: String,
    vistorias: List<Vistoria>,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
    onVistoriaClick: (Vistoria) -> Unit = {}
) {
    val totais = vistorias.size
    val emAndamento = vistorias.count { it.status == StatusVistoria.EM_ANDAMENTO }
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
            .padding(horizontal = 24.dp)
    ) {
        // Parte fixa
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(saudacaoPorHorario(), fontSize = 14.sp, color = TextGray)
                Text(nomeUsuario, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NavyBlue)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(iniciais, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard("$totais", "Vistorias totais", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            StatCard("$emAndamento", "Em andamento", Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            StatCard("$concluidas", "Concluídas", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Suas vistorias", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Parte que rola
        if (vistorias.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma vistoria cadastrada ainda.", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(vistorias) { vistoria ->
                    VistoriaCard(
                        vistoria = vistoria,
                        onClick = { onVistoriaClick(vistoria) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun VistoriaCard(vistoria: Vistoria, onClick: () -> Unit) {
    val icone = when {
        vistoria.titulo.contains("Edifício", ignoreCase = true) ||
                vistoria.titulo.contains("apto", ignoreCase = true) -> Icons.Filled.Apartment
        vistoria.titulo.contains("Sala", ignoreCase = true) ||
                vistoria.titulo.contains("Comercial", ignoreCase = true) -> Icons.Filled.Storefront
        else -> Icons.Filled.House
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .cardBorder()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightBlueBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = NavyBlue)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(vistoria.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(vistoria.endereco, fontSize = 13.sp, color = TextGray)
            }

            StatusBadge(vistoria.status)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = vistoria.data,
            fontSize = 12.sp,
            color = TextGray,
            modifier = Modifier.align(Alignment.End)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Apartment,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${vistoria.ambientesFeitos} de ${vistoria.ambientesTotais} ambientes",
                    fontSize = 13.sp,
                    color = TextGray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(vistoria.responsavel, fontSize = 13.sp, color = TextGray)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: StatusVistoria) {
    val (texto, bg, textColor) = when (status) {
        StatusVistoria.EM_ANDAMENTO -> Triple("Em andamento", StatusOrangeBg, StatusOrangeText)
        StatusVistoria.CONCLUIDA -> Triple("Concluída", StatusGreenBg, StatusGreenText)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}