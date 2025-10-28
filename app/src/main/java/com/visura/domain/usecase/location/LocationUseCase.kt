package com.visura.domain.usecase.location

import android.Manifest
import androidx.annotation.RequiresPermission
import com.visura.data.repository.location.LocationRepository
import javax.inject.Inject

class LocationUseCase @Inject constructor(private val repository: LocationRepository) {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun fetchAddress() =
        repository.fetchAddress()
    suspend fun fetchAddressByName(query: String) =
        repository.fetchAddressByName(query)
}