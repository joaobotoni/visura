package com.example.firstproject.ui.screens.pessoas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.ui.screens.home.cardBorder
import com.example.firstproject.ui.screens.imoveis.PapelPessoa
import com.example.firstproject.ui.screens.imoveis.PessoaVinculada
import com.example.firstproject.ui.theme.BorderGray
import com.example.firstproject.ui.theme.LightBlueBg
import com.example.firstproject.ui.theme.NavyBlue
import com.example.firstproject.ui.theme.ScrimBg
import com.example.firstproject.ui.theme.StatusGreenBg
import com.example.firstproject.ui.theme.StatusGreenText
import com.example.firstproject.ui.theme.TextGray
import com.example.firstproject.ui.theme.clickableNoRipple

// Papel que a próxima pessoa adicionada vai receber: primeiro o proprietário, depois o inquilino
internal fun papelQueFalta(pessoas: List<PessoaVinculada>): PapelPessoa? = when {
    pessoas.none { it.papel == PapelPessoa.PROPRIETARIO } -> PapelPessoa.PROPRIETARIO
    pessoas.none { it.papel == PapelPessoa.INQUILINO } -> PapelPessoa.INQUILINO
    else -> null
}

// Painel que sobe do rodapé sobre um fundo escurecido; tocar fora ou voltar fecha
@Composable
internal fun PainelInferior(
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(onBack = onClose)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimBg)
            .clickableNoRipple(onClose)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = maxHeight * 0.82f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .clickableNoRipple { } // impede que toques no painel fechem o sheet
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun PessoaCard(
    pessoa: PessoaVinculada,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .cardBorder()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightBlueBg),
            contentAlignment = Alignment.Center
        ) {
            Text(pessoa.iniciais, color = NavyBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(pessoa.nome, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (pessoa.documento.isNotBlank()) {
                Text(pessoa.documento, fontSize = 12.sp, color = TextGray)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        PapelChip(pessoa.papel)
    }
}

@Composable
internal fun PapelChip(papel: PapelPessoa) {
    val (fundo, cor) = when (papel) {
        PapelPessoa.PROPRIETARIO -> StatusGreenBg to StatusGreenText
        PapelPessoa.INQUILINO -> LightBlueBg to NavyBlue
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(papel.rotulo, fontSize = 11.sp, color = cor, fontWeight = FontWeight.Bold)
    }
}
