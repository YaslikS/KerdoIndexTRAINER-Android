package com.amed.kerdoindextrainer.fireBaseManagers

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService

// функция отслеживания интернета
@Suppress("DEPRECATION")
fun hasConnection(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

    if (wifiInfo != null && wifiInfo.isConnected) return true

    wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
    if (wifiInfo != null && wifiInfo.isConnected) return true
    wifiInfo = cm.activeNetworkInfo

    return if (wifiInfo != null && wifiInfo.isConnected) true
        else false
}