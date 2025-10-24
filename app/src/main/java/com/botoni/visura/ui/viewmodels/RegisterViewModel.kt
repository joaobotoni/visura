package com.botoni.visura.ui.viewmodels

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botoni.visura.domain.exceptions.location.LocationException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class RegisterUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
@HiltViewModel
class RegisterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun fetchCurrentAddress() {
        viewModelScope.launch {
            setLoadingState(true)
            try {
                val location = getCurrentLocation()
                val addresses = geocodeLocation(location)
                updateAddresses(addresses)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                setLoadingState(false)
            }
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private suspend fun getCurrentLocation(): Location {
        val cancellationTokenSource = CancellationTokenSource()

        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await() ?: throw LocationException.LocationNotFoundException()
        } catch (e: Exception) {
            cancellationTokenSource.cancel()
            throw e
        }
    }

    private suspend fun geocodeLocation(location: Location): List<Address> {
        return withContext(Dispatchers.IO) {
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                5
            )

            if (addresses.isNullOrEmpty()) {
                throw LocationException.AddressNotFoundException()
            }

            addresses
        }
    }

    private fun updateAddresses(addresses: List<Address>) {
        _uiState.update { it.copy(addresses = addresses, error = null) }
    }

    private fun setLoadingState(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun handleError(exception: Exception) {
        val errorMessage = when (exception) {
            is LocationException.LocationNotFoundException -> "Localização não disponível"
            is LocationException.AddressNotFoundException -> "Endereço não encontrado"
            else -> "Erro ao obter localização: ${exception.message}"
        }
        _uiState.update { it.copy(error = errorMessage) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun searchAddress(query: String) {
        if (query.length < 3) {
            _uiState.update { it.copy(addresses = emptyList()) }
            return
        }

        viewModelScope.launch {
            setLoadingState(true)
            try {
                val addresses = geocodeAddressByName(query)
                updateAddresses(addresses)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private suspend fun geocodeAddressByName(locationName: String): List<Address> {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocationName(locationName, 5)
                if (addresses.isNullOrEmpty()) {
                    throw LocationException.AddressNotFoundException()
                }
                addresses
            } catch (e: Exception) {
                throw LocationException.AddressNotFoundException(e)
            }
        }
    }
}