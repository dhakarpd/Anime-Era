package com.github.dhakarpd.animeera.util

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission

object InternetConnectivityChecker {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnectedToInternet(context: Context) : Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}