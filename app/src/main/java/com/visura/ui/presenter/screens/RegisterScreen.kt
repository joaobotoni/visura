package com.visura.ui.presenter.screens

import android.Manifest
import android.location.Address
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.visura.R
import com.visura.ui.presenter.elements.button.StandardTextButton
import com.visura.ui.viewmodels.RegisterViewModel
import com.visura.ui.viewmodels.RegisterUiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Register(viewModel: RegisterViewModel = hiltViewModel()) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    RequirePermissionLocation(
        onPermissionsGranted = { viewModel.fetchCurrentAddress() },
        onPermissionsDenied = { viewModel.clearError() }
    )

    Scaffold(
        floatingActionButton = {
            LocationButton(onClick = { showBottomSheet = true })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (showBottomSheet) {
                LocationBottomSheet(
                    uiState = uiState,
                    onSearch = { query -> viewModel.searchAddress(query) },
                    onCurrentLocationClick =  { viewModel.fetchCurrentAddress() },
                    onAddressClick = { address ->
                        showBottomSheet = false
                        // TODO: Handle address selection
                    },
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.clearError()
                    }
                )
            }
        }
    }
}

@Composable
private fun LocationButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationBottomSheet(
    uiState: RegisterUiState,
    onSearch: (String) -> Unit,
    onCurrentLocationClick: () -> Unit,
    onAddressClick: (Address) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var query by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            LocationSearchBar(
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery
                    onSearch(newQuery)
                },
                onCurrentLocationClick = onCurrentLocationClick
            )

            SearchResultsList(
                visible = query.isNotEmpty() || uiState.isLoading || uiState.addresses.isNotEmpty(),
                addresses = uiState.addresses,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onAddressClick = onAddressClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCurrentLocationClick: () -> Unit
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { },
                expanded = false,
                onExpandedChange = { },
                placeholder = {
                    Text(
                        text = "Digite sua localização",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Localização atual",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onCurrentLocationClick)
                    )
                }
            )
        },
        expanded = false,
        onExpandedChange = { },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {}
}

@Composable
private fun SearchResultsList(
    visible: Boolean,
    addresses: Set<Address>,
    isLoading: Boolean,
    error: String?,
    onAddressClick: (Address) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(error = error)
                addresses.isEmpty() -> EmptyState()
                else -> AddressList(addresses = addresses, onAddressClick = onAddressClick)
            }
        }
    }
}

@Composable
private fun AddressList(
    addresses: Set<Address>,
    onAddressClick: (Address) -> Unit
) {
    val addressList = remember(addresses) { addresses.toList() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = addressList,
            key = { address -> address.hashCode() }
        ) { address ->
            AddressItem(
                address = address,
                onClick = { onAddressClick(address) }
            )
            if (address != addressList.last()) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun AddressItem(
    address: Address,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAddressPrimaryText(address),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildAddressSecondaryText(address),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Buscando endereços...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(error: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Erro",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nenhum endereço encontrado",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildAddressPrimaryText(address: Address): String {
    val street = address.thoroughfare
    val neighborhood = address.subLocality
    return when {
        street != null && neighborhood != null -> "$street, $neighborhood"
        street != null -> street
        neighborhood != null -> neighborhood
        else -> address.locality ?: "Endereço"
    }
}

private fun buildAddressSecondaryText(address: Address): String {
    val parts = mutableListOf<String>()
    address.subThoroughfare?.let { parts.add(it) }
    address.locality?.let { parts.add(it) }
    address.adminArea?.let { parts.add(it) }
    address.postalCode?.let { parts.add("CEP $it") }
    return parts.joinToString(separator = ", ")
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequirePermissionLocation(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    if (locationPermissionsState.shouldShowRationale) {
        PermissionRationaleDialog(
            onRequestPermission = {
                locationPermissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = onPermissionsDenied
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_location_error_title)) },
        text = { Text(stringResource(R.string.permission_location_error_body)) },
        confirmButton = {
            StandardTextButton(
                text = "Permitir",
                onClick = onRequestPermission,
                enabled = true
            )
        },
        dismissButton = {
            StandardTextButton(
                text = "Agora não",
                onClick = onDismiss,
                enabled = true,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
