package com.example.remoteappdemo

import android.net.nsd.NsdServiceInfo

interface NetWorkDiscoveryInterface {
    fun onServiceFound(serviceInfo: NsdServiceInfo) = Unit
    fun onResolveService(serviceInfo: NsdServiceInfo) = Unit
    fun onItemClick(position:Int) = Unit



}