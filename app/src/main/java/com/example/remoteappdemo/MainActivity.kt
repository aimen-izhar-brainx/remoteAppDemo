package com.example.remoteappdemo

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remoteappdemo.adapter.DiscoverDevicesAdapter
import com.example.remoteappdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(),NetWorkDiscoveryInterface {
    var binding: ActivityMainBinding? = null
    private lateinit var devicesAdapter : DiscoverDevicesAdapter
    private var discoverDevices :ArrayList<NsdServiceInfo> = arrayListOf()
    private var resolveDevices :ArrayList<NsdServiceInfo> = arrayListOf()
    lateinit var wifi : WifiManager
    lateinit var multicastLock : WifiManager.MulticastLock
    private lateinit var networkDiscovery :NetworkDiscovery
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        devicesAdapter = DiscoverDevicesAdapter(adapterActionListener)
        setAdapter()
        networkDiscovery = NetworkDiscovery(this, this)
        networkDiscovery.startDiscovery()

    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        super.onServiceFound(serviceInfo)
        lifecycleScope.launch(Dispatchers.Main) {
            discoverDevices.add(serviceInfo)
              devicesAdapter.setData(discoverDevices)
              devicesAdapter.notifyDataSetChanged()

        }
    }

    private suspend fun resolveServicesSequentially(devices: List<NsdServiceInfo>) {
        withContext(Dispatchers.IO) {
            devices.forEach { device ->
                networkDiscovery.resolveService(device)
            }
        }
    }

    private val adapterActionListener = object : NetWorkDiscoveryInterface {
        override fun onItemClick(position: Int) {
            super.onItemClick(position)
            networkDiscovery.resolveService(discoverDevices.get(position))

        }
    }

    override fun onResolveService(serviceInfo: NsdServiceInfo) {
        super.onResolveService(serviceInfo)
        lifecycleScope.launch(Dispatchers.Main) {
            resolveDevices.add(serviceInfo)
            /* devicesAdapter.setData(resolveDevices)
            devicesAdapter.notifyDataSetChanged()*/
            Toast.makeText(
                this@MainActivity,
                serviceInfo.serviceName + " " + serviceInfo.host.hostAddress,
                Toast.LENGTH_LONG
            ).show()

            lifecycleScope.launch(Dispatchers.IO) {
                val connection = Connection(this@MainActivity)
                val socket = serviceInfo.host.hostAddress?.let {
                    connection.connectToDevice(
                        it,
                        serviceInfo.port
                    )
                }
            }
        }
          /*  Toast.makeText(
                this@MainActivity,
                socket.toString(),
                Toast.LENGTH_LONG
            ).show()*/
        }

    private fun setAdapter() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.apply {
            reverseLayout = false
            stackFromEnd = false
        }
        binding?.rcv?.apply {
            layoutManager = linearLayoutManager
            setHasFixedSize(false)
            itemAnimator = DefaultItemAnimator()
            adapter = devicesAdapter

        }
    }
}

