package com.visura.ui.presenter.screens

import android.Manifest
import android.location.Address
import androidx.annotation.RequiresPermission
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.*
import com.visura.R
import com.visura.domain.model.property.*
import com.visura.ui.presenter.elements.button.StandardTextButton
import com.visura.ui.presenter.elements.snackbar.*
import com.visura.ui.viewmodels.*
import kotlinx.coroutines.launch
import java.util.*

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Register(viewModel: RegisterViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is RegisterEvent.Error -> scope.launch { snackbarHostState.showSnackbar(event.message) }
                is RegisterEvent.ValidationSuccess -> showConfirmDialog = true
                else -> Unit
            }
        }
    }

    LocationPermissionRequester { viewModel.fetchCurrentAddress() }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.selectedAddress != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                FloatingActionButton(
                    onClick = {  },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Edit, "Editar localização", Modifier.size(24.dp))
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.isFormComplete,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                FinishButton { viewModel.validateAndFinish() }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val listState = rememberLazyListState()
            val isAtTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 } }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { HeaderSection(if (state.selectedAddress != null) 2 else 1) }
                item { PropertyTypeCard(state.selectedProperty, viewModel::setProperty) }
                item { PropertyCategoryCard(state.selectedPropertyCategory, viewModel::setPropertyCategory) }

                item {
                    AnimatedVisibility(
                        visible = state.selectedAddress != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        state.selectedAddress?.let { SelectedAddressCard(it) { viewModel.setAddress(null) } }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = state.selectedAddress == null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val canAdd = state.selectedProperty != null && state.selectedPropertyCategory != null
                        EmptyLocationCard(canAdd) { if (canAdd) showBottomSheet = true }
                    }
                }

                item { ProcessStepsCard() }
            }

            if (!isAtTop) {
                Box(
                    Modifier.fillMaxWidth().height(16.dp)
                        .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface.copy(0.9f), Color.Transparent)))
                )
            }

            if (showBottomSheet) {
                LocationSearchBottomSheet(
                    state = state,
                    onSearch = viewModel::setSearchQuery,
                    onAddressSelected = { viewModel.setAddress(it); showBottomSheet = false },
                    onDismiss = { showBottomSheet = false }
                )
            }

            if (showConfirmDialog && state.selectedAddress != null) {
                ConfirmationDialog(
                    property = state.selectedProperty,
                    category = state.selectedPropertyCategory,
                    address = state.selectedAddress!!,
                    onConfirm = { showConfirmDialog = false },
                    onDismiss = { showConfirmDialog = false }
                )
            }

            StandardSnackbar(
                hostState = snackbarHostState,
                type = SnackbarType.ERROR,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// =============================
// SEÇÃO DE CABEÇALHO
// =============================
@Composable
private fun HeaderSection(step: Int) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Cadastre o Imóvel", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Preencha as informações básicas para começar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        ProgressCard(step)
    }
}

@Composable
private fun ProgressCard(currentStep: Int) {
    val progress = currentStep / 3f
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Etapa $currentStep de 3", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
                    Text("${(currentStep * 100 / 3)}%", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

// =============================
// BOTÃO FINALIZAR
// =============================
@Composable
private fun FinishButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Finalizar Cadastro", fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
        }
    }
}

// =============================
// CARD DE LOCALIZAÇÃO VAZIA
// =============================
@Composable
private fun EmptyLocationCard(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp).clip(CircleShape).background(if (enabled) MaterialTheme.colorScheme.primary.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.25f)), Alignment.Center) {
                if (enabled) {
                    PulsingLocationIcon()
                } else {
                    Icon(imageVector = Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier =  Modifier.size(48.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                if (enabled) "Adicione a Localização" else "Aguardando Seleção",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (enabled) "Toque aqui para buscar ou usar sua localização atual" else "Selecione o tipo e categoria do imóvel primeiro",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(if (enabled) 0.8f else 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            if (enabled) {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.TouchApp, null, tint = MaterialTheme.colorScheme.primary.copy(0.6f), modifier = Modifier.size(16.dp))
                    Text("Toque para continuar", color = MaterialTheme.colorScheme.primary.copy(0.7f), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PulsingLocationIcon() {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(1f, 1.15f, infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val alpha by transition.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    Box(Modifier.size(80.dp).scale(scale).alpha(alpha).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) {
        Icon(imageVector = Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
    }
}

// =============================
// CARD DE ENDEREÇO SELECIONADO
// =============================
@Composable
private fun SelectedAddressCard(address: Address, onRemove: () -> Unit) {
    Surface(Modifier.fillMaxWidth().height(88.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer, tonalElevation = 2.dp) {
        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary), Alignment.Center) {
                Icon(imageVector = Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimary, modifier =  Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Endereço Selecionado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.7f), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(formatAddressPrimary(address), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRemove, Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Filled.Close, "Remover", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// =============================
// PASSO A PASSO
// =============================
@Composable
private fun ProcessStepsCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(24.dp)) {
            Text("Como Funciona", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            ProcessStep(Icons.Outlined.Home, "Defina o Tipo", "Escolha se é residencial, comercial ou não residencial")
            ProcessStep(Icons.Outlined.LocationOn, "Adicione a Localização", "Busque ou use sua localização atual")
            ProcessStep(Icons.Outlined.Check, "Finalize o Cadastro", "Complete as informações e salve", isLast = true)
        }
    }
}

@Composable
private fun ProcessStep(icon: ImageVector, title: String, description: String, isLast: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer))), Alignment.Center) {
                Icon(imageVector = icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            if (!isLast) Box(Modifier.width(2.dp).height(48.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f).padding(bottom = if (!isLast) 16.dp else 0.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// =============================
// SELETORES DE TIPO E CATEGORIA
// =============================
@Composable
private fun PropertyTypeCard(selected: Property?, onSelect: (Property) -> Unit) {
    val options = PropertyType.entries.map { Property(it) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon( imageVector = Icons.Outlined.Home, null, tint = MaterialTheme.colorScheme.primary, modifier =  Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Tipo do Imóvel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            options.forEach { property ->
                PropertyTypeOption(property, selected == property) { onSelect(property) }
            }
        }
    }
}

@Composable
private fun PropertyTypeOption(property: Property, isSelected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, tween(200))
    Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick), color = bg, tonalElevation = if (isSelected) 2.dp else 0.dp) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(isSelected, onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.width(12.dp))
            Text(property.type.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PropertyCategoryCard(selected: PropertyCategory?, onSelect: (PropertyCategory) -> Unit) {
    val options = PropertyCategoryType.entries.map { PropertyCategory(it) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Category, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Categoria", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            options.forEach { category ->
                PropertyCategoryOption(category, selected == category) { onSelect(category) }
            }
        }
    }
}

@Composable
private fun PropertyCategoryOption(category: PropertyCategory, isSelected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, tween(200))
    Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick), color = bg, tonalElevation = if (isSelected) 2.dp else 0.dp) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(isSelected, onClick, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = if (category.type == PropertyCategoryType.HOME) Icons.Outlined.Home else Icons.Outlined.Apartment,
                null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(category.type.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
        }
    }
}

// =============================
// BOTTOM SHEET DE BUSCA
// =============================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchBottomSheet(
    state: RegisterState,
    onSearch: (String) -> Unit,
    onAddressSelected: (Address) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { Box(Modifier.padding(vertical = 12.dp).size(width = 40.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))) }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 24.dp)) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it; onSearch(it) },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Digite sua localização", maxLines = 1, fontSize = 16.sp, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {}

            if (query.isNotEmpty() || state.isSearching || state.isFetchingLocation || state.addresses.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SearchResultsContent(state, onAddressSelected)
            }
        }
    }
}

@Composable
private fun SearchResultsContent(state: RegisterState, onSelect: (Address) -> Unit) {
    when {
        state.isSearching || state.isFetchingLocation -> LoadingContent()
        state.addresses.isEmpty() && state.searchQuery.isNotEmpty() -> EmptyContent()
        else -> AddressResultsList(state.addresses.toList(), onSelect)
    }
}

@Composable
private fun LoadingContent() {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
        Spacer(Modifier.height(16.dp))
        Text("Buscando endereços...", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyContent() {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier =  Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Nenhum endereço encontrado", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Text("Tente buscar com outros termos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun AddressResultsList(addresses: List<Address>, onSelect: (Address) -> Unit) {
    LazyColumn {
        items(addresses, key = { it.hashCode() }) { address ->
            AddressResultItem(address) { onSelect(address) }
            if (address != addresses.last()) HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
        }
    }
}

@Composable
private fun AddressResultItem(address: Address, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) {
            Icon( imageVector = Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(formatAddressPrimary(address), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(formatAddressSecondary(address), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// =============================
// PERMISSÃO
// =============================
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionRequester(onGranted: () -> Unit) {
    val permissions = rememberMultiplePermissionsState(listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))

    LaunchedEffect(permissions.allPermissionsGranted) { if (permissions.allPermissionsGranted) onGranted() }
    LaunchedEffect(Unit) { if (!permissions.allPermissionsGranted) permissions.launchMultiplePermissionRequest() }

    if (permissions.shouldShowRationale) {
        AlertDialog(
            onDismissRequest = { permissions.launchMultiplePermissionRequest() },
            title = { Text(stringResource(R.string.permission_location_error_title)) },
            text = { Text(stringResource(R.string.permission_location_error_body)) },
            confirmButton = { StandardTextButton("Permitir", { permissions.launchMultiplePermissionRequest() }, enabled = true) }
        )
    }
}

// =============================
// DIÁLOGO DE CONFIRMAÇÃO
// =============================
@Composable
private fun ConfirmationDialog(
    property: Property?,
    category: PropertyCategory?,
    address: Address,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog (onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().padding(vertical = 20.dp), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(0.2f)).border(1.5.dp, MaterialTheme.colorScheme.primary.copy(0.3f), CircleShape), Alignment.Center) {
                    Icon(imageVector = Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    property?.let { PropertyBadge(it.type.displayName.titleCase()) }
                    Spacer(Modifier.width(8.dp))
                    category?.let { CategoryBadge(it.type.displayName.titleCase()) }
                }
                Spacer(Modifier.height(24.dp))
                AddressSummary(address)
                Spacer(Modifier.height(28.dp))
                ActionButtons(onConfirm, onDismiss)
            }
        }
    }
}

private fun String.titleCase(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

@Composable
private fun PropertyBadge(text: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 2.dp) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

@Composable
private fun CategoryBadge(text: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, tonalElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon( imageVector = when {
                text.contains("Casa") -> Icons.Outlined.Home
                else -> Icons.Outlined.Apartment
            }, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun AddressSummary(address: Address) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Informações do Endereço", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
        AddressRow("Endereço", address.thoroughfare)
        AddressRow("Número", address.subThoroughfare)
        AddressRow("Bairro", address.subLocality)
        AddressRow("Cidade", address.locality)
        AddressRow("Estado", address.adminArea)
        AddressRow("CEP", address.postalCode)
    }
}

@Composable
private fun AddressRow(label: String, value: String?) {
    value?.let {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$label:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(16.dp))
            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun ActionButtons(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onDismiss, Modifier.weight(1f).height(48.dp), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.6f))) {
            Text("Cancelar", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = onConfirm, Modifier.weight(1f).height(48.dp), shape = MaterialTheme.shapes.medium, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)) {
            Text("Confirmar", fontWeight = FontWeight.SemiBold)
        }
    }
}

// =============================
// UTILITÁRIOS DE TEXTO
// =============================
private fun formatAddressPrimary(address: Address): String {
    val street = address.thoroughfare
    val neighborhood = address.subLocality
    return when {
        street != null && neighborhood != null -> "$street, $neighborhood"
        street != null -> street
        neighborhood != null -> neighborhood
        else -> address.locality ?: "Endereço"
    }
}

private fun formatAddressSecondary(address: Address): String {
    val parts = mutableListOf<String>()
    address.subThoroughfare?.let { parts.add(it) }
    address.locality?.let { parts.add(it) }
    address.adminArea?.let { parts.add(it) }
    address.postalCode?.let { parts.add("CEP $it") }
    return parts.joinToString(", ")
}