package com.visura.ui.viewmodels

import android.Manifest
import android.location.Address
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visura.domain.exceptions.location.LocationException
import com.visura.domain.usecase.location.LocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val addresses: Set<Address> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedResidenceType: String = "Residencial",
    val selectedAddress: Address? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val locationUseCase: LocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchCurrentAddress() {
        viewModelScope.launch {
            startLoading()
            fetchLocation()
        }
    }

    fun searchAddress(query: String) {
        if (isQueryTooShort(query)) {
            clearAddresses()
            return
        }

        viewModelScope.launch {
            startLoading()
            searchByName(query)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateResidenceType(type: String) {
        _uiState.update { it.copy(selectedResidenceType = type) }
    }

    fun updateSelectedAddress(address: Address?) {
        _uiState.update { it.copy(selectedAddress = address) }
    }

    private fun isQueryTooShort(query: String): Boolean {
        return query.length < 3
    }

    private fun clearAddresses() {
        _uiState.update { it.copy(addresses = emptySet(), error = null) }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun fetchLocation() {
        runCatching {
            locationUseCase.fetchAddress()
        }.onSuccess { addresses ->
            showAddresses(addresses)
        }.onFailure { error ->
            showError(error)
        }
    }

    private suspend fun searchByName(query: String) {
        runCatching {
            locationUseCase.fetchAddressByName(query)
        }.onSuccess { addresses ->
            showAddresses(addresses)
        }.onFailure { error ->
            showError(error)
        }
    }

    private fun showAddresses(addresses: List<Address>) {
        _uiState.update {
            it.copy(
                addresses = addresses.toSet(),
                isLoading = false,
                error = null
            )
        }
    }

    private fun startLoading() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    private fun showError(error: Throwable) {
        val message = buildErrorMessage(error)
        _uiState.update {
            it.copy(error = message, isLoading = false)
        }
    }

    private fun buildErrorMessage(error: Throwable): String {
        return when (error) {
            is LocationException.LocationNotFoundException -> "Localização não disponível"
            is LocationException.AddressNotFoundException -> "Endereço não encontrado"
            else -> "Erro ao obter localização: ${error.message}"
        }
    }
}