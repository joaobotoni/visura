package com.visura.data.repository.location

import android.Manifest
import androidx.annotation.RequiresPermission
import com.visura.data.datasource.location.LocationLocalDataSource
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationDatasource: LocationLocalDataSource
) {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun fetchAddress() =
        locationDatasource.fetchCurrentAddress()

    suspend fun fetchAddressByName(query: String) =
        locationDatasource.fetchCurrentAddressByName(query)
}