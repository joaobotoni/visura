package com.visura.data.datasource.location

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun fetchLastLocation(): Location? =
        fusedLocationClient.lastLocation.await()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun fetchCurrentLastLocation(): Location? =
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()

    private suspend fun fetchAddressFromLocation(location: Location): List<Address> =
        suspendCoroutine { continuation ->
            geocoder.getFromLocation(location.latitude, location.longitude, 5)
            { addresses -> continuation.resume(addresses) }
        }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun fetchCurrentAddress(): List<Address> {
        val location = fetchCurrentLastLocation() ?: return emptyList()
        return fetchAddressFromLocation(location)
    }

    private suspend fun fetchAddressByName(query: String): List<Address> =
        suspendCoroutine { continuation ->
            geocoder.getFromLocationName(query, 5)
            { addresses -> continuation.resume(addresses) }
        }

    suspend fun fetchCurrentAddressByName(query: String): List<Address> =
        fetchAddressByName(query)

}
