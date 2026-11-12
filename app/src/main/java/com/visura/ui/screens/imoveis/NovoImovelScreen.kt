package com.visura.ui.screens.imoveis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

data class EnderecoImovel(
    val cep: String,
    val rua: String,
    val numero: String,
    val complemento: String,
    val bairro: String,
    val cidade: String,
    val uf: String
)

@Composable
fun NovoImovelScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: (EnderecoImovel) -> Unit = {}
) {
    var cep by rememberSaveable { mutableStateOf("") }
    var rua by rememberSaveable { mutableStateOf("") }
    var numero by rememberSaveable { mutableStateOf("") }
    var complemento by rememberSaveable { mutableStateOf("") }
    var bairro by rememberSaveable { mutableStateOf("") }
    var cidade by rememberSaveable { mutableStateOf("") }
    var uf by rememberSaveable { mutableStateOf("") }
    var mostrarBusca by rememberSaveable { mutableStateOf(false) }

    // Complemento é opcional; os demais campos são obrigatórios
    val podeContinuar = cep.length == 9 && rua.isNotBlank() && numero.isNotBlank() &&
            bairro.isNotBlank() && cidade.isNotBlank() && uf.length == 2

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.White) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .safeDrawingPadding()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dados do imóvel", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Preencha as informações de localização do imóvel ou busque por proximidade.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Onde fica o imóvel?", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        CampoEndereco(
                            label = "CEP",
                            value = cep,
                            onValueChange = { novo ->
                                val digitos = novo.filter { it.isDigit() }.take(8)
                                cep = if (digitos.length > 5) {
                                    digitos.substring(0, 5) + "-" + digitos.substring(5)
                                } else digitos
                            },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(0.9f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        CampoEndereco(
                            label = "Rua",
                            value = rua,
                            onValueChange = { rua = it },
                            capitalization = KeyboardCapitalization.Words,
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        CampoEndereco(
                            label = "Número",
                            value = numero,
                            onValueChange = { numero = it },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        CampoEndereco(
                            label = "Complemento",
                            value = complemento,
                            onValueChange = { complemento = it },
                            capitalization = KeyboardCapitalization.Sentences,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        CampoEndereco(
                            label = "Bairro",
                            value = bairro,
                            onValueChange = { bairro = it },
                            capitalization = KeyboardCapitalization.Words,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        CampoEndereco(
                            label = "Cidade",
                            value = cidade,
                            onValueChange = { cidade = it },
                            capitalization = KeyboardCapitalization.Words,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        CampoEndereco(
                            label = "UF",
                            value = uf,
                            onValueChange = { novo ->
                                uf = novo.filter { it.isLetter() }.take(2).uppercase()
                            },
                            capitalization = KeyboardCapitalization.Characters,
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                HorizontalDivider(color = BorderGray)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = { mostrarBusca = true },
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = NavyBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Buscar endereço",
                            color = NavyBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onContinueClick(
                                EnderecoImovel(
                                    cep = cep,
                                    rua = rua.trim(),
                                    numero = numero.trim(),
                                    complemento = complemento.trim(),
                                    bairro = bairro.trim(),
                                    cidade = cidade.trim(),
                                    uf = uf
                                )
                            )
                        },
                        enabled = podeContinuar,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        if (mostrarBusca) {
            BuscarEnderecoOverlay(
                onClose = { mostrarBusca = false },
                onConfirm = { endereco ->
                    cep = endereco.cep
                    rua = endereco.rua
                    numero = endereco.numero
                    bairro = endereco.bairro
                    cidade = endereco.cidade
                    uf = endereco.uf
                    mostrarBusca = false
                }
            )
        }
    }
}

@Composable
private fun CampoEndereco(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = TextGray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderGray,
                focusedBorderColor = NavyBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}