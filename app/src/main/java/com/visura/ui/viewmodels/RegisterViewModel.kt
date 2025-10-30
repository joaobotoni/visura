package com.visura.ui.viewmodels

import android.Manifest
import android.content.Context
import android.location.Address
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visura.R
import com.visura.domain.model.property.PropertyCategory
import com.visura.domain.model.property.Property
import com.visura.domain.usecase.location.LocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val addresses: Set<Address> = emptySet(),
    val selectedAddress: Address? = null,
    val selectedPropertyCategory: PropertyCategory? = null,
    val selectedProperty: Property? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isFetchingLocation: Boolean = false
) {
    val isFormComplete: Boolean
        get() = selectedPropertyCategory != null &&
                selectedProperty != null &&
                selectedAddress != null
}

sealed interface RegisterEvent {
    data class Success(val message: String) : RegisterEvent
    data class Error(val message: String) : RegisterEvent
    data object ValidationSuccess : RegisterEvent
}

class RegisterValidator @Inject constructor() {

    fun validate(state: RegisterState): Result<Triple<PropertyCategory, Property, Address>> = runCatching {
        val property = validatePropertyCategory(state.selectedPropertyCategory)
        val residence = validateProperty(state.selectedProperty)
        val address = validateAddress(state.selectedAddress)
        Triple(property, residence, address)
    }

    private fun validatePropertyCategory(selectedPropertyCategory: PropertyCategory?): PropertyCategory {
        return PropertyCategory.access(selectedPropertyCategory?.type).getOrThrow()
    }

    private fun validateProperty(selectedProperty: Property?): Property {
        return Property.access(selectedProperty?.type).getOrThrow()
    }

    private fun validateAddress(selectedAddress: Address?): Address {
        return selectedAddress ?: throw IllegalArgumentException("Endereço não selecionado")
    }
}

class RegisterEventMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun toSuccess(): RegisterEvent.Success =
        RegisterEvent.Success(context.getString(R.string.success_message_registration))

    fun toError(exception: Throwable): RegisterEvent.Error =
        RegisterEvent.Error(exception.message ?: context.getString(R.string.unknown_error_message))
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val locationUseCase: LocationUseCase,
    private val validator: RegisterValidator,
    private val mapper: RegisterEventMapper
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<RegisterEvent>()
    val event = _event.asSharedFlow()

    fun setPropertyCategory(propertyCategory: PropertyCategory?) {
        _state.update { it.copy(selectedPropertyCategory = propertyCategory) }
    }

    fun setProperty(property: Property?) {
        _state.update { it.copy(selectedProperty = property) }
    }

    fun setAddress(address: Address?) {
        _state.update { it.copy(selectedAddress = address) }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length < 3) {
            _state.update { it.copy(addresses = emptySet()) }
        } else {
            searchAddress(query)
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun fetchCurrentAddress() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isFetchingLocation = true) }
                currentLocationFetch()
            } finally {
                _state.update { it.copy(isFetchingLocation = false) }
            }
        }
    }

    fun searchAddress(query: String) {
        if (query.length < 3) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isSearching = true) }
                addressSearch(query)
            } finally {
                _state.update { it.copy(isSearching = false) }
            }
        }
    }
    fun validateAndFinish() {
        viewModelScope.launch {
            send(performValidation())
        }
    }
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private suspend fun currentLocationFetch() {
        runCatching {
            locationUseCase.fetchAddress()
        }.fold(
            onSuccess = { addresses ->
                _state.update { it.copy(addresses = addresses.toSet()) }
            },
            onFailure = { send(mapper.toError(it)) }
        )
    }

    private suspend fun addressSearch(query: String) {
        runCatching {
            locationUseCase.fetchAddressByName(query)
        }.fold(
            onSuccess = { addresses ->
                _state.update { it.copy(addresses = addresses.toSet()) }
            },
            onFailure = { send(mapper.toError(it)) }
        )
    }

    private fun performValidation(): RegisterEvent =
        validator.validate(_state.value).fold(
            onSuccess = { RegisterEvent.ValidationSuccess },
            onFailure = { mapper.toError(it) }
        )

    private suspend fun send(event: RegisterEvent) {
        _event.emit(event)
    }
}