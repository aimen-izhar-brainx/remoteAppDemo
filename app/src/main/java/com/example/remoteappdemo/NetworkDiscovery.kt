package com.example.remoteappdemo

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class NetworkDiscovery(private val context: Context, private val listener: NetWorkDiscoveryInterface) {

    private var nsdManager: NsdManager? = null
    private val resolveListenerMap = mutableMapOf<String, NsdManager.ResolveListener>()
    private val serviceDiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
            listener.onServiceFound(serviceInfo)

            // Handle discovered service here
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
            resolveListenerMap.remove(serviceInfo.serviceName)
            // Handle lost service here
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG, "Service discovery stopped")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error $errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Stop discovery failed: Error $errorCode")
        }
    }
    //_privet, _http-alt, _http,connect,_pdl-datastream, uscans,uscan,scanner,_androidtvremote2._tcp",_services._dns-sd._udp
    fun startDiscovery() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager?
        nsdManager?.discoverServices("_androidtvremote2._tcp", NsdManager.PROTOCOL_DNS_SD, serviceDiscoveryListener)
    }

    fun stopDiscovery() {
        nsdManager?.stopServiceDiscovery(serviceDiscoveryListener)
    }

    fun resolveService(serviceInfo: NsdServiceInfo) {
        val executor: Executor = Executors.newSingleThreadExecutor()
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            nsdManager?.registerServiceInfoCallback(serviceInfo, executor, serviceInfoCallback)
        } else {*/
        nsdManager?.resolveService(serviceInfo, resolveListener)
        //}
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")
            Log.d(TAG, "IP Address: ${serviceInfo.host.hostAddress}, Port: ${serviceInfo.port}")
            listener.onResolveService(serviceInfo)

        }
    }

/*    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    var serviceInfoCallback: NsdManager.ServiceInfoCallback =
    object : NsdManager.ServiceInfoCallback {

        override fun onServiceInfoCallbackRegistrationFailed(p0: Int) {
        }

        override fun onServiceUpdated(p0: NsdServiceInfo) {
           // listener.onServiceFound(p0)
            listener.onResolveService(p0)
        }

        override fun onServiceLost() {
        }

        override fun onServiceInfoCallbackUnregistered() {
        }
    }*/
    companion object {
        private const val TAG = "NetworkDiscovery"
    }
}