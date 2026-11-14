package com.visura.ui.screens.createaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

enum class TipoPessoa { FISICA, JURIDICA }

// Papel de quem cadastra a empresa. A API só aceita papéis de administrador nesse cadastro
// (funcionário entra depois, convidado por um administrador). valorApi é o "role" da API.
enum class PapelNaEmpresa(val rotulo: String, val valorApi: String) {
    SOCIO("Sócio", "socio"),
    REPRESENTANTE("Representante legal", "representante"),
    MEI("Titular MEI", "mei")
}

// Dados da segunda etapa do cadastro. Os campos da empresa só valem para pessoa jurídica.
data class DocumentosCadastro(
    val nome: String,
    val cpf: String,
    val tipoPessoa: TipoPessoa,
    val razaoSocial: String,
    val nomeFantasia: String,
    val cnpj: String,
    val papel: PapelNaEmpresa?
)

@Composable
fun CreateAccountDocsScreen(
    nomeInicial: String = "",
    onBackClick: () -> Unit = {},
    onFinishClick: (DocumentosCadastro) -> Unit = {}
) {
    var tipoPessoa by rememberSaveable { mutableStateOf(TipoPessoa.FISICA) }
    var nomeCompleto by rememberSaveable { mutableStateOf(nomeInicial) }
    var cpf by rememberSaveable { mutableStateOf("") }
    var razaoSocial by rememberSaveable { mutableStateOf("") }
    var nomeFantasia by rememberSaveable { mutableStateOf("") }
    var cnpj by rememberSaveable { mutableStateOf("") }
    var papel by rememberSaveable { mutableStateOf<PapelNaEmpresa?>(null) }

    val cpfCompleto = cpf.count { it.isDigit() } == 11
    val cnpjCompleto = cnpj.count { it.isDigit() } == 14

    val podeConcluir = if (tipoPessoa == TipoPessoa.FISICA) {
        nomeCompleto.isNotBlank() && cpfCompleto
    } else {
        nomeCompleto.isNotBlank() && cpfCompleto &&
                razaoSocial.isNotBlank() && nomeFantasia.isNotBlank() && cnpjCompleto && papel != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.padding(bottom = 4.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }

        Text("Documentos", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Você será automaticamente o responsável por todas as vistorias criadas.",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("TIPO DE PESSOA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                text = "Pessoa física",
                icon = Icons.Filled.Person,
                selected = tipoPessoa == TipoPessoa.FISICA,
                onClick = { tipoPessoa = TipoPessoa.FISICA },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            SegmentedButton(
                text = "Pessoa jurídica",
                icon = Icons.Filled.Business,
                selected = tipoPessoa == TipoPessoa.JURIDICA,
                onClick = { tipoPessoa = TipoPessoa.JURIDICA },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("IDENTIFICAÇÃO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(8.dp))

        DocField(
            label = "Nome completo *",
            value = nomeCompleto,
            onValueChange = { nomeCompleto = it },
            icon = Icons.Filled.Person
        )

        Spacer(modifier = Modifier.height(12.dp))

        DocField(
            label = "CPF *",
            value = cpf,
            onValueChange = { if (it.length <= 14) cpf = it },
            icon = Icons.Filled.Badge,
            keyboardType = KeyboardType.Number,
            showCheck = cpfCompleto
        )

        if (tipoPessoa == TipoPessoa.JURIDICA) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderGray)
            Spacer(modifier = Modifier.height(16.dp))

            DocField(
                label = "Razão social *",
                value = razaoSocial,
                onValueChange = { razaoSocial = it },
                icon = Icons.Filled.Business
            )

            Spacer(modifier = Modifier.height(12.dp))

            DocField(
                label = "Nome fantasia *",
                value = nomeFantasia,
                onValueChange = { nomeFantasia = it },
                icon = Icons.Filled.Storefront
            )

            Spacer(modifier = Modifier.height(12.dp))

            DocField(
                label = "CNPJ *",
                value = cnpj,
                onValueChange = { if (it.length <= 18) cnpj = it },
                icon = Icons.Filled.Badge,
                keyboardType = KeyboardType.Number,
                showCheck = cnpjCompleto
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("SEU PAPEL NA EMPRESA *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PapelNaEmpresa.entries.forEach { opcao ->
                    FilterChip(
                        selected = papel == opcao,
                        onClick = { papel = opcao },
                        label = { Text(opcao.rotulo, fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Você será administrador da empresa e poderá convidar outros membros.",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onFinishClick(
                    DocumentosCadastro(
                        nome = nomeCompleto.trim(),
                        cpf = cpf.filter { it.isDigit() },
                        tipoPessoa = tipoPessoa,
                        razaoSocial = razaoSocial.trim(),
                        nomeFantasia = nomeFantasia.trim(),
                        cnpj = cnpj.filter { it.isDigit() },
                        papel = papel.takeIf { tipoPessoa == TipoPessoa.JURIDICA }
                    )
                )
            },
            enabled = podeConcluir,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Concluir cadastro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SegmentedButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            modifier = modifier.height(44.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
            modifier = modifier.height(44.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 13.sp)
        }
    }
}

@Composable
fun DocField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    showCheck: Boolean = false
) {
    Text(label, fontSize = 13.sp, color = TextGray)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextGray) },
        trailingIcon = if (showCheck) {
            { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32)) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = NavyBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
}