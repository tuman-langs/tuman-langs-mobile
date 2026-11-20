package ru.itmo.tuman.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

interface InternetAvailability {
    fun hasInternet(): Boolean
}

class AndroidInternetAvailability(context: Context) : InternetAvailability {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun hasInternet(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
